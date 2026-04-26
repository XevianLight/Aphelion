package net.xevianlight.aphelion.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.xevianlight.aphelion.client.DestinationClientCache;
import net.xevianlight.aphelion.client.PartitionClientState;
import net.xevianlight.aphelion.core.saveddata.types.PartitionData;
import net.xevianlight.aphelion.network.packet.PlanetInfo;
import net.xevianlight.aphelion.network.packet.SetDestinationPayload;
import net.xevianlight.aphelion.network.packet.SetTravelingPayload;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StationFlightComputerScreen extends AbstractContainerScreen<StationFlightComputerMenu> {

    // ── Orbital animation ──────────────────────────────────────────────────
    /** Shared epoch so planet positions persist across screen open/close. */
    private static final long   EPOCH_MS        = System.currentTimeMillis();
    /** Real-world ms for one Earth orbit. Inner planets scale by AU^1.5. */
    private static final double EARTH_PERIOD_MS = 120_000.0; // 15 seconds
    /** Fixed screen-space orbit radius for moons around their parent dot (system view). */
    private static final int    MOON_ORBIT_R      = 13;
    /** Screen-space orbit radius for moons when the orrery is zoomed into a subsystem. */
    private static final int    ZOOM_MOON_ORBIT_R = 45;
    /** Fixed animation period for all moons — actual AU is too small for Keplerian scaling. */
    private static final double MOON_PERIOD_MS    = 18_000.0;

    // ── Layout ─────────────────────────────────────────────────────────────
    private static final int ORRERY_W  = 176;
    private static final int ORRERY_H  = 186;
    private static final int ORRERY_CX = ORRERY_W / 2;   // 88
    private static final int ORRERY_CY = ORRERY_H / 2;   // 93
    private static final int MAX_ORBIT_R = 82;
    private static final int MIN_ORBIT_R = 18;
    private static final int INFO_X = 180;   // relative to leftPos
    private static final int INFO_W = 98;

    // ── Colors ─────────────────────────────────────────────────────────────
    private static final int C_BG           = 0xFF0D0D1A;
    private static final int C_SPACE        = 0xFF030308;
    private static final int C_BORDER       = 0xFF2A2A4A;
    private static final int C_PANEL        = 0xFF0A0A16;
    private static final int C_GOLD         = 0xFFFFD700;
    private static final int C_WHITE        = 0xFFE8E8E8;
    private static final int C_GRAY         = 0xFF888899;
    private static final int C_ORBIT        = 0xFF1A1A30;
    private static final int C_ORBIT_CUR    = 0xFF334466;
    private static final int C_ORBIT_DEST   = 0xFF2A4A2A;
    private static final int C_ORBIT_SEL    = 0xFF2A3A6A;
    private static final int C_STAR         = 0xFFFFE866;
    private static final int C_STATION      = 0xFFFFFFFF;
    private static final int C_TRAVEL_LINE  = 0xFF446644;
    private static final int C_PROG_BG      = 0xFF0A1A0A;
    private static final int C_PROG_FILL    = 0xFF00BB44;

    private static final int[] PLANET_COLORS = {
            0xFFE8A060, 0xFF88CC44, 0xFF4499FF, 0xFFCC8844,
            0xFF88AACC, 0xFFCC44CC, 0xFFFFCC44, 0xFF99FFAA,
    };

    // ── Background stars (fixed, deterministic) ────────────────────────────
    private static final int   STAR_COUNT = 60;
    private static final int[] STAR_PX    = new int[STAR_COUNT];
    private static final int[] STAR_PY    = new int[STAR_COUNT];
    private static final int[] STAR_COL   = new int[STAR_COUNT];
    static {
        long s = 0x5DEECE66DL;
        for (int i = 0; i < STAR_COUNT; i++) {
            s = (s * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFL;
            STAR_PX[i] = (int)(s % ORRERY_W);
            s = (s * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFL;
            STAR_PY[i] = (int)(s % ORRERY_H);
            int alpha = 0x50 + (i % 4) * 0x18;
            STAR_COL[i] = (alpha << 24) | 0xCCCCCC;
        }
    }

    // ── State ──────────────────────────────────────────────────────────────
    @Nullable private ResourceLocation selectedPlanet = null;
    /** Non-null when the orrery is zoomed into a planet's local subsystem. */
    @Nullable private ResourceLocation zoomedSystem   = null;

    /** Cached each frame in renderBg; read in mouseClicked to stay in sync. */
    private final List<int[]> planetDrawCache = new ArrayList<>();  // [absX, absY, screenR, colorIdx]
    private final List<PlanetInfo> planetCache = new ArrayList<>();

    private Button setDestButton;
    private Button travelButton;

    public StationFlightComputerScreen(StationFlightComputerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 280;
        this.imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY     = 10000;
        this.inventoryLabelY = 10000;

        int bx = leftPos + INFO_X + 4;
        int bw = INFO_W - 8;

        setDestButton = addRenderableWidget(Button.builder(
                Component.literal("Set Destination"),
                btn -> {
                    if (selectedPlanet != null) {
                        PacketDistributor.sendToServer(new SetDestinationPayload(
                                menu.blockEntity.getBlockPos(),
                                Optional.of(selectedPlanet)));
                    }
                })
                .pos(bx, topPos + 152)
                .size(bw, 16)
                .build());

        travelButton = addRenderableWidget(Button.builder(
                Component.literal("Launch"),
                btn -> PacketDistributor.sendToServer(new SetTravelingPayload(
                        menu.blockEntity.getBlockPos(),
                        !menu.isTraveling())))
                .pos(bx, topPos + 170)
                .size(bw, 16)
                .build());
    }

    // ── Rendering ──────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Outer frame
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, C_BG);

        drawOrrery(g, mouseX, mouseY);
        drawInfoPanel(g, mouseX, mouseY);

        // Dynamic button labels / states
        travelButton.setMessage(menu.isTraveling()
                ? Component.literal("Abort Travel")
                : Component.literal("Launch"));
        setDestButton.active = selectedPlanet != null;
    }

    // ── Orrery ─────────────────────────────────────────────────────────────

    private void drawOrrery(GuiGraphics g, int mouseX, int mouseY) {
        int orrX = leftPos + 2;
        int orrY = topPos + 2;
        int cx   = orrX + ORRERY_CX;
        int cy   = orrY + ORRERY_CY;

        // Space background
        g.fill(orrX, orrY, orrX + ORRERY_W, orrY + ORRERY_H, C_SPACE);

        // Panel border
        g.fill(orrX,              orrY,              orrX + ORRERY_W,      orrY + 1,          C_BORDER);
        g.fill(orrX,              orrY + ORRERY_H - 1, orrX + ORRERY_W,   orrY + ORRERY_H,  C_BORDER);
        g.fill(orrX,              orrY,              orrX + 1,             orrY + ORRERY_H,  C_BORDER);
        g.fill(orrX + ORRERY_W - 1, orrY,            orrX + ORRERY_W,      orrY + ORRERY_H,  C_BORDER);

        // Background stars
        for (int i = 0; i < STAR_COUNT; i++) {
            g.fill(orrX + STAR_PX[i], orrY + STAR_PY[i],
                   orrX + STAR_PX[i] + 1, orrY + STAR_PY[i] + 1, STAR_COL[i]);
        }

        List<PlanetInfo> allPlanets = DestinationClientCache.get();
        PartitionData data = PartitionClientState.get().map(p -> p.partitionData()).orElse(null);

        // Filter to the station's star system. Resolved client-side so the server
        // can send all planets without needing to know the station's current system.
        ResourceLocation currentSystem = null;
        if (data != null && data.getOrbit() != null) {
            var currentPlanet = net.xevianlight.aphelion.planet.PlanetCache.getByOrbitOrNull(data.getOrbit());
            if (currentPlanet != null) currentSystem = currentPlanet.system().location();
        }
        final ResourceLocation systemFilter = currentSystem;
        List<PlanetInfo> planets = systemFilter == null ? allPlanets : allPlanets.stream()
                .filter(p -> {
                    var planet = net.xevianlight.aphelion.planet.PlanetCache.getOrNull(p.id());
                    return planet == null || planet.system().location().equals(systemFilter);
                })
                .collect(java.util.stream.Collectors.toList());

        // Rebuild cached positions
        planetDrawCache.clear();
        planetCache.clear();

        if (planets.isEmpty()) {
            var font = Minecraft.getInstance().font;
            String msg = "No planet data";
            g.drawString(font, msg, cx - font.width(msg) / 2, cy - 4, C_GRAY, false);
            drawDot(g, cx, cy, 4, C_STAR);
            return;
        }

        long elapsed = System.currentTimeMillis() - EPOCH_MS;

        if (zoomedSystem == null) {
            // ── SYSTEM VIEW ────────────────────────────────────────────────
            // Exclude moons from the star-scale min/max so inner planets aren't crushed.
            double maxOrbit = planets.stream().filter(p -> !p.isMoon()).mapToDouble(PlanetInfo::orbitDistance).max().orElse(1);
            double minOrbit = planets.stream().filter(p -> !p.isMoon()).mapToDouble(PlanetInfo::orbitDistance).min().orElse(0);

            // Pass 1 — non-moon bodies: positions relative to the central star.
            int nonMoonCount = (int) planets.stream().filter(p -> !p.isMoon()).count();
            int phaseSlot = 0;
            for (int i = 0; i < planets.size(); i++) {
                PlanetInfo p = planets.get(i);
                if (p.isMoon()) {
                    planetDrawCache.add(null); // placeholder; filled in pass 2
                    planetCache.add(p);
                    continue;
                }
                double phase  = (2.0 * Math.PI * phaseSlot / Math.max(1, nonMoonCount));
                double period = EARTH_PERIOD_MS * Math.pow(p.orbitDistance(), 1.5);
                double angle  = phase + elapsed * 2.0 * Math.PI / period;
                int screenR   = orbitRadius(p.orbitDistance(), minOrbit, maxOrbit);
                int px        = cx + (int)(screenR * Math.cos(angle));
                int py        = cy + (int)(screenR * Math.sin(angle));
                planetDrawCache.add(new int[]{px, py, screenR, i % PLANET_COLORS.length});
                planetCache.add(p);
                phaseSlot++;
            }

            // Pass 2 — moons: positions relative to their parent's draw position.
            // moonSlotByParent tracks per-parent slot so phase offsets spread siblings evenly.
            java.util.Map<java.util.Optional<ResourceLocation>, Integer> moonSlotByParent = new java.util.HashMap<>();
            for (int i = 0; i < planets.size(); i++) {
                PlanetInfo p = planets.get(i);
                if (!p.isMoon()) continue;
                int parentIdx = indexById(p.parentPlanet().orElse(null));
                int[] parentPos = (parentIdx >= 0) ? planetDrawCache.get(parentIdx) : null;
                int ocx = (parentPos != null) ? parentPos[0] : cx;
                int ocy = (parentPos != null) ? parentPos[1] : cy;
                int slot     = moonSlotByParent.getOrDefault(p.parentPlanet(), 0);
                int siblings = (int) planets.stream().filter(q -> q.isMoon() && q.parentPlanet().equals(p.parentPlanet())).count();
                double phase = (2.0 * Math.PI * slot / Math.max(1, siblings));
                double angle = phase + elapsed * 2.0 * Math.PI / MOON_PERIOD_MS;
                int px = ocx + (int)(MOON_ORBIT_R * Math.cos(angle));
                int py = ocy + (int)(MOON_ORBIT_R * Math.sin(angle));
                planetDrawCache.set(i, new int[]{px, py, MOON_ORBIT_R, i % PLANET_COLORS.length});
                moonSlotByParent.put(p.parentPlanet(), slot + 1);
            }
        } else {
            // ── SUBSYSTEM VIEW ─────────────────────────────────────────────
            // Focused planet sits at orrery centre; its moons orbit it at ZOOM_MOON_ORBIT_R.
            // Every other body is hidden (null entry → unclickable, not drawn).
            java.util.Map<java.util.Optional<ResourceLocation>, Integer> moonSlotByParent = new java.util.HashMap<>();
            for (int i = 0; i < planets.size(); i++) {
                PlanetInfo p = planets.get(i);
                planetCache.add(p);
                if (p.id().equals(zoomedSystem)) {
                    // The focused planet itself: place it at the centre.
                    planetDrawCache.add(new int[]{cx, cy, 0, i % PLANET_COLORS.length});
                } else if (p.isMoon() && p.parentPlanet().map(zoomedSystem::equals).orElse(false)) {
                    // Moon of the focused planet: orbit around centre.
                    int slot     = moonSlotByParent.getOrDefault(p.parentPlanet(), 0);
                    int siblings = (int) planets.stream().filter(q -> q.isMoon() && q.parentPlanet().equals(p.parentPlanet())).count();
                    double phase = (2.0 * Math.PI * slot / Math.max(1, siblings));
                    double angle = phase + elapsed * 2.0 * Math.PI / MOON_PERIOD_MS;
                    int px = cx + (int)(ZOOM_MOON_ORBIT_R * Math.cos(angle));
                    int py = cy + (int)(ZOOM_MOON_ORBIT_R * Math.sin(angle));
                    planetDrawCache.add(new int[]{px, py, ZOOM_MOON_ORBIT_R, i % PLANET_COLORS.length});
                    moonSlotByParent.put(p.parentPlanet(), slot + 1);
                } else {
                    planetDrawCache.add(null);
                }
            }
        }

        // Identify current and destination planets
        ResourceLocation curOrbit = data != null ? data.getOrbit() : null;
        ResourceLocation destId   = data != null ? data.getDestination() : null;

        int curIdx  = indexByOrbit(curOrbit);
        int destIdx = indexById(destId);

        // ── Orbit rings ────────────────────────────────────────────────────
        for (int i = 0; i < planets.size(); i++) {
            int[] pos = planetDrawCache.get(i);
            if (pos == null) continue;
            PlanetInfo p = planets.get(i);
            boolean isCur  = i == curIdx;
            boolean isDest = i == destIdx;
            boolean isSel  = p.id().equals(selectedPlanet);
            int col = isCur ? C_ORBIT_CUR : isDest ? C_ORBIT_DEST : isSel ? C_ORBIT_SEL : C_ORBIT;
            if (p.isMoon()) {
                int parentIdx = indexById(p.parentPlanet().orElse(null));
                int[] parentPos = (parentIdx >= 0) ? planetDrawCache.get(parentIdx) : null;
                int ocx = (parentPos != null) ? parentPos[0] : cx;
                int ocy = (parentPos != null) ? parentPos[1] : cy;
                drawOrbitRing(g, ocx, ocy, pos[2], col); // pos[2] holds the orbit radius (system vs zoom)
            } else if (pos[2] > 0) {
                drawOrbitRing(g, cx, cy, pos[2], col);   // pos[2] == 0 for the focused planet in zoom mode
            }
        }

        // ── Travel arc ─────────────────────────────────────────────────────
        if (data != null && data.isTraveling() && curIdx >= 0 && destIdx >= 0) {
            int[] sp = planetDrawCache.get(curIdx);
            int[] dp = planetDrawCache.get(destIdx);
            if (sp != null && dp != null)
                drawDashedLine(g, sp[0], sp[1], dp[0], dp[1], C_TRAVEL_LINE);
        }

        // ── Central body (star in system view; focused planet in subsystem view) ──
        if (zoomedSystem == null) {
            drawOrbitRing(g, cx, cy, 7, 0x20FFE866);
            drawDot(g, cx, cy, 4, C_STAR);
        } else {
            // Draw focused planet at centre with a glow ring, and show a back hint.
            int focusedIdx = indexById(zoomedSystem);
            int focusedColor = (focusedIdx >= 0) ? PLANET_COLORS[planetDrawCache.get(focusedIdx)[3]] : C_WHITE;
            drawOrbitRing(g, cx, cy, 9, focusedColor & 0x40FFFFFF);
            drawDot(g, cx, cy, 5, focusedColor);
            var font = Minecraft.getInstance().font;
            String hint = "↩ system view";
            g.drawString(font, hint, orrX + 4, orrY + ORRERY_H - 10, C_GRAY, false);
        }

        // ── Planet dots ────────────────────────────────────────────────────
        var font = Minecraft.getInstance().font;
        for (int i = 0; i < planets.size(); i++) {
            PlanetInfo p   = planets.get(i);
            int[]      pos = planetDrawCache.get(i);
            if (pos == null) continue;
            int px = pos[0], py = pos[1];

            boolean isCur   = i == curIdx;
            boolean isDest  = i == destIdx;
            boolean isSel   = p.id().equals(selectedPlanet);
            boolean isHover = distSq(mouseX, mouseY, px, py) <= 64;

            int baseColor = PLANET_COLORS[pos[3]];
            int dotColor  = isSel  ? blend(baseColor, 0xFFFFFFFF, 0.5f)
                          : isDest ? blend(baseColor, 0xFF44FF44, 0.5f)
                          : isCur  ? blend(baseColor, 0xFFFFFFFF, 0.3f)
                          : baseColor;
            int dotR = (isSel || isCur || isDest) ? 4 : 3;
            drawDot(g, px, py, dotR, dotColor);

            // Label: always for current/dest/selected, on hover otherwise
            if (isCur || isDest || isSel || isHover) {
                String name  = formatId(p.id());
                int labelX   = px + dotR + 3;
                int labelY   = py - 4;
                if (labelX + font.width(name) > orrX + ORRERY_W - 4)
                    labelX = px - font.width(name) - dotR - 2;
                int labelCol = isCur ? 0xFFAAAAFF : isDest ? 0xFF88FF88 : isSel ? C_WHITE : C_GRAY;
                g.drawString(font, name, labelX, labelY, labelCol, false);
            }
        }

        // ── Station marker ─────────────────────────────────────────────────
        if (data != null) {
            if (data.isTraveling() && curIdx >= 0 && destIdx >= 0) {
                int[] sp = planetDrawCache.get(curIdx);
                int[] dp = planetDrawCache.get(destIdx);
                if (sp != null && dp != null) {
                    double tripDist = Math.abs(data.getTripDistanceAU());
                    float  progress = tripDist > 0
                            ? (float)(Math.abs(data.getDistanceTraveledAU()) / tripDist) : 0f;
                    progress = Math.max(0f, Math.min(1f, progress));
                    int sx = (int)(sp[0] + (dp[0] - sp[0]) * progress);
                    int sy = (int)(sp[1] + (dp[1] - sp[1]) * progress);
                    drawStation(g, sx, sy, C_STATION);
                }
            } else if (curIdx >= 0) {
                int[] pos = planetDrawCache.get(curIdx);
                if (pos != null) {
                    PlanetInfo curPlanet = planetCache.get(curIdx);
                    int ocx = cx, ocy = cy;
                    if (curPlanet.isMoon()) {
                        int parentIdx = indexById(curPlanet.parentPlanet().orElse(null));
                        int[] parentPos = (parentIdx >= 0) ? planetDrawCache.get(parentIdx) : null;
                        if (parentPos != null) { ocx = parentPos[0]; ocy = parentPos[1]; }
                    }
                    // Offset slightly along the orbit ring so station doesn't overlap the planet dot
                    double baseAngle = Math.atan2(pos[1] - ocy, pos[0] - ocx);
                    double stAngle   = baseAngle + 0.35;
                    int sx = ocx + (int)(pos[2] * Math.cos(stAngle));
                    int sy = ocy + (int)(pos[2] * Math.sin(stAngle));
                    drawStation(g, sx, sy, C_STATION);
                }
            }
        }
    }

    // ── Info panel ─────────────────────────────────────────────────────────

    private void drawInfoPanel(GuiGraphics g, int mouseX, int mouseY) {
        var font = Minecraft.getInstance().font;
        int ix = leftPos + INFO_X;
        int iy = topPos + 2;

        g.fill(ix, iy, ix + INFO_W, iy + ORRERY_H, C_PANEL);
        // Divider from orrery
        g.fill(ix - 2, iy + 6, ix - 1, iy + ORRERY_H - 6, C_BORDER);

        int tx = ix + 5;
        int ty = iy + 6;

        // Title
        String title = "COMPUTER";
        g.drawString(font, title, ix + (INFO_W - font.width(title)) / 2, ty, C_GOLD, false);
        ty += 10;
        g.fill(tx, ty, ix + INFO_W - 5, ty + 1, C_BORDER);
        ty += 5;

        PartitionData data = PartitionClientState.get().map(p -> p.partitionData()).orElse(null);

        // Resolve current planet for system + orbit display
        var currentPlanet = (data != null && data.getOrbit() != null)
                ? net.xevianlight.aphelion.planet.PlanetCache.getByOrbitOrNull(data.getOrbit())
                : null;

        // System
        String systemName = currentPlanet != null ? formatId(currentPlanet.system().location()) : "—";
        g.drawString(font, "System:", tx, ty, C_GRAY, false);
        ty += 9;
        g.drawString(font, systemName, tx + 2, ty, 0xFFCCCCFF, false);
        ty += 11;

        // Orbit (last segment only — strips the "orbit/" prefix)
        String orbitName = (data != null && data.getOrbit() != null) ? formatLastSegment(data.getOrbit()) : "—";
        g.drawString(font, "Orbit:", tx, ty, C_GRAY, false);
        ty += 9;
        g.drawString(font, orbitName, tx + 2, ty, 0xFFAAAAFF, false);
        ty += 11;

        // Destination (committed from PartitionData, or preview from selection)
        boolean hasDest = data != null && data.getDestination() != null;
        ResourceLocation destId = hasDest ? data.getDestination() : selectedPlanet;
        String destName = destId != null ? formatId(destId) : "—";
        int destColor = hasDest ? 0xFF88FF88 : selectedPlanet != null ? C_GOLD : C_GRAY;
        g.drawString(font, "Dest:", tx, ty, C_GRAY, false);
        ty += 9;
        g.drawString(font, destName, tx + 2, ty, destColor, false);
        ty += 10;

        // Distance to destination (shown whenever a destination is selected or committed)
        if (destId != null && data != null) {
            double currentAU = data.getOrbitDistance();
            double destAU = currentAU; // fallback
            for (PlanetInfo pi : planetCache) {
                if (pi.id().equals(destId)) { destAU = pi.orbitDistance(); break; }
            }
            double dist = Math.abs(destAU - currentAU);
            String distLabel = dist < 0.01
                    ? "%.4f AU to dest".formatted(dist)
                    : "%.2f AU to dest".formatted(dist);
            g.drawString(font, distLabel, tx + 2, ty, C_GRAY, false);
            ty += 10;
        }
        ty += 2;

        g.fill(tx, ty, ix + INFO_W - 5, ty + 1, C_BORDER);
        ty += 4;

        // Engines & Pads
        g.drawString(font, "Engines: " + menu.getEngineCount(), tx, ty, C_GRAY, false);
        ty += 10;
        g.drawString(font, "Pads:    " + menu.getPadCount(), tx, ty, C_GRAY, false);
        ty += 13;

        g.fill(tx, ty, ix + INFO_W - 5, ty + 1, C_BORDER);
        ty += 4;

        // Travel status
        boolean traveling = data != null && data.isTraveling();
        g.drawString(font, traveling ? "TRAVELING" : "IDLE", tx, ty, traveling ? C_GOLD : C_GRAY, false);
        ty += 11;

        if (traveling) {
            double tripDist = Math.abs(data.getTripDistanceAU());
            float progress = tripDist > 0
                    ? (float)(Math.abs(data.getDistanceTraveledAU()) / tripDist) : 0f;
            progress = Math.max(0f, Math.min(1f, progress));

            int bw = INFO_W - 10;
            g.fill(tx, ty, tx + bw, ty + 7, C_PROG_BG);
            if (progress > 0) g.fill(tx, ty, tx + (int)(bw * progress), ty + 7, C_PROG_FILL);
            g.fill(tx, ty, tx + bw, ty + 1, C_BORDER);
            g.fill(tx, ty, tx + 1, ty + 7, C_BORDER);
            ty += 10;

            String progStr = "%.2f / %.2f AU".formatted(
                    Math.abs(data.getDistanceTraveledAU()),
                    Math.abs(data.getTripDistanceAU()));
            g.drawString(font, progStr, tx, ty, C_GRAY, false);
        }
    }

    // ── Input ──────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int orrX = leftPos + 2;
        int orrY = topPos + 2;

        if (button == 0 && mouseX >= orrX && mouseX < orrX + ORRERY_W
                && mouseY >= orrY && mouseY < orrY + ORRERY_H) {
            // Find nearest body within 10px
            ResourceLocation nearest = null;
            int nearestDist2 = 100;
            for (int i = 0; i < planetDrawCache.size(); i++) {
                int[] pos = planetDrawCache.get(i);
                if (pos == null) continue;
                int d2 = distSq((int)mouseX, (int)mouseY, pos[0], pos[1]);
                if (d2 < nearestDist2) { nearestDist2 = d2; nearest = planetCache.get(i).id(); }
            }

            if (nearest == null) {
                // Clicked empty space: zoom out if zoomed in.
                zoomedSystem = null;
            } else if (zoomedSystem != null) {
                // In subsystem view: clicking any body selects it.
                selectedPlanet = nearest.equals(selectedPlanet) ? null : nearest;
            } else {
                // In system view: check if this body has moons.
                final ResourceLocation nearestFinal = nearest;
                boolean hasMoons = planetCache.stream().anyMatch(p ->
                        p.isMoon() && p.parentPlanet().map(nearestFinal::equals).orElse(false));
                if (hasMoons) {
                    // Zoom into this planet's subsystem; clear selection to avoid confusion.
                    zoomedSystem = nearest;
                    selectedPlanet = null;
                } else {
                    selectedPlanet = nearest.equals(selectedPlanet) ? null : nearest;
                }
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ── Drawing helpers ────────────────────────────────────────────────────

    private static void drawOrbitRing(GuiGraphics g, int cx, int cy, int r, int color) {
        int steps = Math.max(64, r * 6);
        for (int i = 0; i < steps; i++) {
            double a = 2.0 * Math.PI * i / steps;
            int x = cx + (int)Math.round(r * Math.cos(a));
            int y = cy + (int)Math.round(r * Math.sin(a));
            g.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static void drawDot(GuiGraphics g, int cx, int cy, int r, int color) {
        int r2 = r * r + r;
        for (int dy = -r; dy <= r; dy++)
            for (int dx = -r; dx <= r; dx++)
                if (dx * dx + dy * dy <= r2)
                    g.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
    }

    /** Small cross (station marker). */
    private static void drawStation(GuiGraphics g, int cx, int cy, int color) {
        g.fill(cx - 4, cy,     cx + 5, cy + 1, color);  // horizontal
        g.fill(cx,     cy - 4, cx + 1, cy + 5, color);  // vertical
        // Hollow center for clarity
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFF000000);
        g.fill(cx,     cy,     cx + 1, cy + 1, color);
    }

    private static void drawDashedLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        float dx = x2 - x1, dy = y2 - y1;
        int steps = (int)Math.sqrt(dx * dx + dy * dy);
        if (steps == 0) return;
        for (int i = 0; i <= steps; i++) {
            if ((i / 3) % 2 == 0) {
                int x = x1 + (int)(dx * i / steps);
                int y = y1 + (int)(dy * i / steps);
                g.fill(x, y, x + 1, y + 1, color);
            }
        }
    }

    // ── Utilities ──────────────────────────────────────────────────────────

    // Log scale so inner planets (Mercury, Venus) aren't crushed to the center on a linear AU axis.
    private int orbitRadius(double orbitDist, double minDist, double maxDist) {
        if (maxDist <= minDist) return (MIN_ORBIT_R + MAX_ORBIT_R) / 2;
        double logMin  = Math.log(Math.max(minDist,  0.001));
        double logMax  = Math.log(Math.max(maxDist,  0.001));
        double logDist = Math.log(Math.max(orbitDist, 0.001));
        double t = (logDist - logMin) / (logMax - logMin);
        return (int)(MIN_ORBIT_R + t * (MAX_ORBIT_R - MIN_ORBIT_R));
    }

    private int indexByOrbit(@Nullable ResourceLocation orbitRl) {
        if (orbitRl == null) return -1;
        for (int i = 0; i < planetCache.size(); i++)
            if (planetCache.get(i).orbit().equals(orbitRl)) return i;
        return -1;
    }

    private int indexById(@Nullable ResourceLocation planetId) {
        if (planetId == null) return -1;
        for (int i = 0; i < planetCache.size(); i++)
            if (planetCache.get(i).id().equals(planetId)) return i;
        return -1;
    }

    private static int distSq(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2, dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    private static int blend(int base, int target, float t) {
        int ba = (base >> 24) & 0xFF, br = (base >> 16) & 0xFF,
            bg = (base >> 8)  & 0xFF, bb = base & 0xFF;
        int ta = (target >> 24) & 0xFF, tr = (target >> 16) & 0xFF,
            tg = (target >> 8)  & 0xFF, tb = target & 0xFF;
        return ((int)(ba + (ta - ba) * t) << 24)
             | ((int)(br + (tr - br) * t) << 16)
             | ((int)(bg + (tg - bg) * t) << 8)
             |  (int)(bb + (tb - bb) * t);
    }

    private static String formatId(ResourceLocation id) {
        String[] parts = id.getPath().split("[_/]");
        var sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    /** Like formatId but only uses the last path segment, so orbit/earth → "Earth". */
    private static String formatLastSegment(ResourceLocation id) {
        String path = id.getPath();
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        String[] parts = name.split("_");
        var sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}
