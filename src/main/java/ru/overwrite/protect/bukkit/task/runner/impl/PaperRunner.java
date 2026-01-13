package ru.overwrite.protect.bukkit.task.runner.impl;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.overwrite.protect.bukkit.ServerProtectorManager;
import ru.overwrite.protect.bukkit.task.runner.Runner;
import ru.overwrite.protect.bukkit.task.task.Task;
import ru.overwrite.protect.bukkit.task.task.impl.PaperTask;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class PaperRunner implements Runner {

    private final ServerProtectorManager plugin;
    private final Plugin taskOwner;
    private final AsyncScheduler asyncScheduler;
    private final GlobalRegionScheduler globalScheduler;

    public PaperRunner(ServerProtectorManager plugin, Plugin taskOwner) {
        this.plugin = plugin;
        this.taskOwner = taskOwner;
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
        this.globalScheduler = plugin.getServer().getGlobalRegionScheduler();
    }

    @Override
    public Task runPlayer(@NotNull Runnable task, @NotNull Player player) {
        return new PaperTask(player.getScheduler().run(taskOwner, toConsumer(task), null));
    }

    @Override
    public Task run(@NotNull Runnable task) {
        return new PaperTask(globalScheduler.run(taskOwner, toConsumer(task)));
    }

    @Override
    public Task runAsync(@NotNull Runnable task) {
        return new PaperTask(asyncScheduler.runNow(taskOwner, toConsumer(task)));
    }

    @Override
    public Task runDelayed(@NotNull Runnable task, long delayTicks) {
        return new PaperTask(globalScheduler.runDelayed(taskOwner, toConsumer(task), delayTicks));
    }

    @Override
    public Task runDelayedAsync(@NotNull Runnable task, long delayTicks) {
        return new PaperTask(asyncScheduler.runDelayed(taskOwner, toConsumer(task), toMilli(delayTicks), TimeUnit.MILLISECONDS));
    }

    @Override
    public Task runPeriodical(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new PaperTask(globalScheduler.runAtFixedRate(taskOwner, toConsumer(task), delayTicks, periodTicks));
    }

    @Override
    public Task runPeriodicalAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new PaperTask(asyncScheduler.runAtFixedRate(taskOwner, toConsumer(task), toMilli(delayTicks), toMilli(periodTicks), TimeUnit.MILLISECONDS));
    }

    @Override
    public void cancelTasks() {
        if (!plugin.isCalledFromAllowedApplication()) {
            return;
        }
        globalScheduler.cancelTasks(taskOwner);
        asyncScheduler.cancelTasks(taskOwner);
    }

    private static Consumer<ScheduledTask> toConsumer(Runnable task) {
        return st -> task.run();
    }

    private static long toMilli(long ticks) {
        return ticks * 50L;
    }
}