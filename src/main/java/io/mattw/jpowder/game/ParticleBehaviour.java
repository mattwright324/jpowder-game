package io.mattw.jpowder.game;

public abstract class ParticleBehaviour {
    /**
     * Initialises the particle - called when the particle is placed.
     *
     * @param p The particle that is being created.
     */
    public abstract void init(Particle p);

    /**
     * Called every frame to update the particle.
     *
     * @param p The particle that is being updated.
     */
    public abstract void update(Particle p);

    /**
     * Called when the particle decays.
     *
     * @param p The particle that is destroyed.
     */
    public void destruct(Particle p) {
    }
}