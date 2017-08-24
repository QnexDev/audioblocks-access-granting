package com.qnex.audioblocks.access.task

class ActionTaskRegistry(tasks: List<ActionTask<*>>) {

    private val actionTasks = hashMapOf<String, ActionTask<*>>()

    init {
        if (tasks.toSet().size == tasks.size) {
            throw IllegalArgumentException("Tasks list contains duplicates!")
        }
        tasks.associateByTo(actionTasks,{ actionTaskFactory -> actionTaskFactory.getName() })
    }


    fun getActionTask(name: String): ActionTask<*> {
        return actionTasks[name] ?: throw IllegalArgumentException("Couldn't find task with this name: $name")
    }

    fun getAllActionTasks(): List<ActionTask<*>> {
        return actionTasks.values.toList()
    }

}