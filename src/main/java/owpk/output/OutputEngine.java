package owpk.output;

public abstract class OutputEngine {

    public synchronized void enhanceOutput(OutputEnhancer outputEnhancer) {
        this.outputEnhancer = outputEnhancer;
    }

    public void out(byte[] message) {

    }

    public void out(String message) {
        
    }

    protected abstract void outDefault(byte[] message);
}
