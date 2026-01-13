package ru.overwrite.protect.bukkit.task.task.impl;

import ru.overwrite.protect.bukkit.task.task.Task;

public class BukkitTask implements Task {

    private final org.bukkit.scheduler.BukkitTask bukkitTask;

    public BukkitTask(org.bukkit.scheduler.BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    @Override
    public void cancel() {
        this.bukkitTask.cancel();
    }
}
