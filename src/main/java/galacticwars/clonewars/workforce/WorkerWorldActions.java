package galacticwars.clonewars.workforce;

/**
 * Narrow server-authoritative world/inventory boundary exposed to profession
 * handlers. Implementations may perform one atomic interaction but must never
 * publish navigation targets.
 */
public interface WorkerWorldActions {
    WorkerActionResult executeAtomicWorkerAction(
            WorkerProfession profession,
            WorkerAction action);
}
