package weather2.util;

import CoroUtil.ai.ITaskInitializer;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;

import java.lang.reflect.Constructor;

/**
 * Use until the difficulty/AI branch from CoroUtil hits production
 */
public class UtilEntityBuffsMini {

    public static boolean replaceTaskIfMissing(EntityCreature ent, Class taskToReplace, Class tasksToReplaceWith, int priorityOfTask) {
        EntityAITasks.EntityAITaskEntry foundTask = null;
        for (Object entry2 : ent.tasks.taskEntries) {
            EntityAITasks.EntityAITaskEntry entry = (EntityAITasks.EntityAITaskEntry) entry2;
            if (taskToReplace.isAssignableFrom(entry.action.getClass())) {
                foundTask = entry;
                break;
            }
        }

        if (foundTask != null) {
            ent.tasks.taskEntries.remove(foundTask);

            addTask(ent, tasksToReplaceWith, priorityOfTask);
        }

        return foundTask != null;

    }

    public static boolean addTask(EntityCreature ent, Class taskToInject, int priorityOfTask) {
        try {
            Constructor<?> cons = taskToInject.getConstructor();
            Object obj = cons.newInstance();
            if (obj instanceof ITaskInitializer) {
                ITaskInitializer task = (ITaskInitializer) obj;
                task.setEntity(ent);
                //System.out.println("adding task into zombie: " + taskToInject);
                ent.tasks.addTask(priorityOfTask, (EntityAIBase) task);
                //aiEnhanced.put(ent.getEntityId(), true);


                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
