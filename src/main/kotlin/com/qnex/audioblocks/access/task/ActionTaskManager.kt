package com.qnex.audioblocks.access.task

import javax.annotation.concurrent.NotThreadSafe


@NotThreadSafe // TODO for now :)
class ActionTaskManager(val actionTaskRegistry: ActionTaskRegistry) {
    private val startedTasks = hashSetOf<String>()
    private val stoppedTasks = hashSetOf<String>()
    private val pausedTasks = hashSetOf<String>()

    fun startTask(name: String) {
        val actionTask = actionTaskRegistry.getActionTask(name)
        if (startedTasks.contains(name)) {
            throw IllegalStateException("This action task already started!")
        }
        if (pausedTasks.contains(name)) {
            throw IllegalStateException("This action task is suspended!")
        }
        if (stoppedTasks.contains(name)) {
            stoppedTasks.remove(name)
        }
        actionTask.start()
        startedTasks.add(actionTask.getName())
    }

    fun stopTask(name: String) {
        val actionTask = actionTaskRegistry.getActionTask(name)
        if (stoppedTasks.contains(name)) {
            throw IllegalStateException("This action task is already stopped!")
        }
        if (pausedTasks.contains(name)) {
            throw IllegalStateException("This action task is already suspended!")
        }
        if (!startedTasks.contains(name)) {
            throw IllegalStateException("This action task has not been started yet!")
        } else {
            startedTasks.remove(name)
        }
        actionTask.stop()
        stoppedTasks.add(actionTask.getName())
    }

    fun pauseTask(name: String) {
        val actionTask = actionTaskRegistry.getActionTask(name)
        if (pausedTasks.contains(name)) {
            throw IllegalStateException("This action task is already suspended!")
        }
        if (stoppedTasks.contains(name)) {
            throw IllegalStateException("This action task is already stopped!")
        }
        if (!startedTasks.contains(name)) {
            throw IllegalStateException("This action task has not been started yet!")
        } else {
            startedTasks.remove(name)
        }
        actionTask.stop()
        pausedTasks.add(actionTask.getName())
    }

    fun continueTask(name: String) {
        val actionTask = actionTaskRegistry.getActionTask(name)
        if (startedTasks.contains(name)) {
            throw IllegalStateException("This action task already started!")
        }
        if (stoppedTasks.contains(name)) {
            throw IllegalStateException("This action task is stopped!")
        }
        if (!pausedTasks.contains(name)) {
            throw IllegalStateException("This action task must be started!")
        } else {
            pausedTasks.remove(name)
        }
        actionTask.stop()
        pausedTasks.add(actionTask.getName())
    }

    fun stopAllTask() {
        val actionTasks = actionTaskRegistry.getAllActionTasks()
        actionTasks
                .filter { startedTasks.contains(it.getName()) }
                .forEach({ actionTask: ActionTask<*> ->
                    run {
                        actionTask.stop()
                        startedTasks.remove(actionTask.getName())
                        stoppedTasks.add(actionTask.getName())
                    }
                })
    }

    fun pauseAllTask() {
        val actionTasks = actionTaskRegistry.getAllActionTasks()
        actionTasks
                .filter { startedTasks.contains(it.getName()) }
                .forEach({ actionTask: ActionTask<*> ->
                    run {
                        actionTask.pause()
                        startedTasks.remove(actionTask.getName())
                        pausedTasks.add(actionTask.getName())
                    }
                })
    }
}