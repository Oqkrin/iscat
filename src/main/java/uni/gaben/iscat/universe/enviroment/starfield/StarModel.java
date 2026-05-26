package uni.gaben.iscat.universe.enviroment.starfield;

public class StarModel {
    private double x, y, size;
    
    public StarModel(double x, double y, double size) {
        this.x = x; 
        this.y = y; 
        this.size = size;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return size; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
