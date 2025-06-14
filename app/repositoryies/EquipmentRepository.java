package repositoryies;

import io.ebean.DB;
import models.Equipment;
import repositoryies.DatabaseExecutionContext;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes equipment database operations in a different
 * execution context.
 */
public class EquipmentRepository {

    private final DatabaseExecutionContext executionContext;

    @Inject
    public EquipmentRepository(DatabaseExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public CompletionStage<Optional<Equipment>> findById(Long id) {
        return supplyAsync(() -> DB.find(Equipment.class).setId(id).findOneOrEmpty(), executionContext);
    }

    public CompletionStage<List<Equipment>> findAll() {
        return supplyAsync(() -> DB.find(Equipment.class).orderBy("name").findList(), executionContext);
    }

    public CompletionStage<Long> insert(Equipment equipment) {
        return supplyAsync(() -> {
            equipment.save();
            return equipment.getId();
        }, executionContext);
    }

    public CompletionStage<Equipment> update(Equipment equipment) {
        return supplyAsync(() -> {
            equipment.update();
            return equipment;
        }, executionContext);
    }

    public CompletionStage<Boolean> delete(Long id) {
        return supplyAsync(() -> {
            Optional<Equipment> equipment = DB.find(Equipment.class).setId(id).findOneOrEmpty();
            if (equipment.isPresent()) {
                equipment.get().delete();
                return true;
            }
            return false;
        }, executionContext);
    }
}