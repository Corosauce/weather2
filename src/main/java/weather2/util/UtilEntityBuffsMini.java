package weather2.util;

import CoroUtil.ai.ITaskInitializer;
import net.minecraft.block.Block;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.world.ServerTickList;

import java.lang.reflect.Constructor;

/**
 * Use until the difficulty/AI branch from CoroUtil hits production
 */
public class UtilEntityBuffsMini {

    public static boolean replaceTaskIfMissing(CreatureEntity ent, Class taskToReplace, Class tasksToReplaceWith, int priorityOfTask) {
        GoalSelector.EntityAITaskEntry foundTask = null;
        for (Object entry2 : ent.goalSelector.goals) {
            GoalSelector.EntityAITaskEntry entry = (GoalSelector.EntityAITaskEntry) entry2;
            if (taskToReplace.isAssignableFrom(entry.action.getClass())) {
                foundTask = entry;
                break;
            }
        }

        ((ServerTickList<Block>)ent.world.getPendingBlockTicks()).getPending()

        if (foundTask != null) {
            ent.goalSelector.goals.remove(foundTask);

            addGoal(ent, tasksToReplaceWith, priorityOfTask);
        }

        return foundTask != null;

    }

    public static boolean addGoal(CreatureEntity ent, Class taskToInject, int priorityOfTask) {
        try {
            Constructor<?> cons = taskToInject.getConstructor();
            Object obj = cons.newInstance();
            if (obj instanceof ITaskInitializer) {
                ITaskInitializer task = (ITaskInitializer) obj;
                task.setEntity(ent);
                //System.out.println("adding task into zombie: " + taskToInject);
                ent.goalSelector.addGoal(priorityOfTask, (Goal) task);
                //aiEnhanced.put(ent.getEntityId(), true);


                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
