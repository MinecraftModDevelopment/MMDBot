package com.mcmoddev.mmdbot.commander.reminders;

import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.migrate.TricksMigrator;
import com.mcmoddev.mmdbot.commander.tricks.Trick;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.data.VersionedDataMigrator;
import com.mcmoddev.mmdbot.core.util.data.VersionedDatabase;
import com.mcmoddev.mmdbot.dashboard.util.LazySupplier;
import lombok.experimental.UtilityClass;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.mcmoddev.mmdbot.core.util.Constants.Gsons.NO_PRETTY_PRINTING;

@UtilityClass
public class Reminders {

    /**
     * The current schema version used for data migration.
     */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    /**
     * The migrator.
     */
    public static final VersionedDataMigrator MIGRATOR = VersionedDataMigrator.builder()
        .build();

    /**
     * The path of the reminders file.
     */
    public static final LazySupplier<Path> PATH = LazySupplier.of(() -> TheCommander.getInstance().getRunPath().resolve("reminders.json"));

    /**
     * The timer that runs reminders.
     */
    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> Utils.setThreadDaemon(new Thread(r, "Reminders"), true));

    private static List<Reminder> reminders;

    public static List<Reminder> getReminders() {
        if (reminders != null) return reminders;
        final var path = PATH.get();
        if (!Files.exists(path)) {
            return reminders = new ArrayList<>();
        }
        final var typeOfList = new TypeToken<List<Reminder>>() {
        }.getType();
        try {
            final var db = VersionedDatabase.<List<Reminder>>fromFile(NO_PRETTY_PRINTING, path, typeOfList, CURRENT_SCHEMA_VERSION, new ArrayList<>());
            if (db.getSchemaVersion() != CURRENT_SCHEMA_VERSION) {
                MIGRATOR.migrate(CURRENT_SCHEMA_VERSION, path);
                final var newDb = VersionedDatabase.<List<Reminder>>fromFile(NO_PRETTY_PRINTING, path, typeOfList);
                return reminders = newDb.getData();
            } else {
                return reminders = db.getData();
            }
        } catch (final IOException exception) {
            TheCommander.LOGGER.error("Failed to read reminders file...", exception);
            return reminders = new ArrayList<>();
        }
    }

    /**
     * Write the reminders to disk.
     */
    private static void write() {
        final var path = PATH.get();
        final var rems = getReminders();
        final var db = VersionedDatabase.inMemory(CURRENT_SCHEMA_VERSION, rems);
        try (var writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
            NO_PRETTY_PRINTING.toJson(db.toJson(NO_PRETTY_PRINTING), writer);
        } catch (final IOException e) {
            TheCommander.LOGGER.error("An IOException occurred saving reminders...", e);
        }
    }

    /**
     * Schedules all the reminders to be run.
     */
    public static void scheduleAllReminders() {
        getReminders().forEach(Reminders::registerReminder);
    }

    /**
     * Removes a reminder.
     * @param reminder the reminder to remove.
     */
    public static void removeReminder(final Reminder reminder) {
        reminder.removed().set(true);
        getReminders().remove(reminder);
        write();
    }

    /**
     * Adds and schedules a reminder.
     * @param reminder the reminder to add.
     */
    public static void addReminder(final Reminder reminder) {
        getReminders().add(reminder);
        registerReminder(reminder);
        write();
    }

    /**
     * Schedules a reminder.
     * @param reminder the reminder to schedule
     */
    public static void registerReminder(final Reminder reminder) {
        Utils.scheduleTask(TIMER, () -> {
            reminder.run();
            removeReminder(reminder);
        }, reminder.time());
    }
}
