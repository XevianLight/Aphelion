package net.xevianlight.aphelion.systems.conveyor;

import net.minecraft.world.item.ItemStack;
import net.xevianlight.aphelion.block.entity.custom.PipeTestBlockEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// I'm naming and structuring the conveyor stuff like this bc i kinda wanna try making a space-engineers
// style request system later... though it might not be necessary, given that the reason it needs to be like that
// in space engineers is mostly space constraints if i had to guess
public class ConveyorNetwork {
    boolean isInvalid = false;
    public List<PipeTestBlockEntity> pipes = new ArrayList<>();
    public Set<ConveyorOutput> outputs = new HashSet<>();

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (isInvalid) return stack;
        for (ConveyorOutput output : outputs) {
            stack = output.insertItem(stack, simulate);
            if (stack.isEmpty()) break;
        }
        return stack;
    }

    public void addPipe(PipeTestBlockEntity pipe) {
        pipes.add(pipe);
        pipe.graph = this;
        for (ConveyorOutput output : pipe.outputs.values()) {
            if (output != null) this.outputs.add(output);
        }
    }

    /// Called whenever a pipe is removed from a graph, or when a new graph comes across an old one.
    public void invalidate() {
        for (PipeTestBlockEntity pipe : pipes) {
            pipe.graph = null;
            for (ConveyorOutput output : pipe.outputs.values()) {
                if (output != null) this.outputs.remove(output);
            }
        }
        this.isInvalid = true;
    }
}
