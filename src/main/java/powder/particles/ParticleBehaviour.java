package powder.particles;

public abstract class ParticleBehaviour {
    /**
     * Initialises the particle - called when the particle is placed.
     *
     * @param p
     */
    public abstract void init(Particle p);

    public abstract void update(Particle p);
}