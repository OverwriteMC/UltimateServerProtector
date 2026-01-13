package ru.overwrite.protect.bukkit.task.runner.impl;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.task.runner.Runner;
import ru.overwrite.protect.bukkit.task.task.Task;
import ru.overwrite.protect.bukkit.task.task.impl.BukkitTask;

@SuppressWarnings("deprecation")
public final class BukkitRunner implements Runner {

    private final ServerProtectorManager plugin;
    private final Plugin taskOwner;
    private final BukkitScheduler scheduler;

    public BukkitRunner(ServerProtectorManager plugin, Plugin taskOwner) {
        this.plugin = plugin;
        this.taskOwner = taskOwner;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public Task runPlayer(@NotNull Runnable task, @NotNull Player player) {
        return new BukkitTask(scheduler.runTask(taskOwner, task));
    }

    @Override
    public Task run(@NotNull Runnable task) {
        return new BukkitTask(scheduler.runTask(taskOwner, task));
    }

    @Override
    public Task runAsync(@NotNull Runnable task) {
        return new BukkitTask(scheduler.runTaskAsynchronously(taskOwner, task));
    }

    @Override
    public Task runDelayed(@NotNull Runnable task, long delayTicks) {
        return new BukkitTask(scheduler.runTaskLater(taskOwner, task, delayTicks));
    }

    @Override
    public Task runDelayedAsync(@NotNull Runnable task, long delayTicks) {
        return new BukkitTask(scheduler.runTaskLaterAsynchronously(taskOwner, task, delayTicks));
    }

    @Override
    public Task runPeriodical(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTask(scheduler.runTaskTimer(taskOwner, task, delayTicks, periodTicks));
    }

    @Override
    public Task runPeriodicalAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTask(scheduler.runTaskTimerAsynchronously(taskOwner, task, delayTicks, periodTicks));
    }

    @Override
    public void cancelTasks() {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        scheduler.cancelTasks(taskOwner);
    }
}