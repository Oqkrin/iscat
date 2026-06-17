package uni.gaben.iscat.universe.entities.interfaces;

public interface Stateful {

    int getState();
    void setState(int state);
    double getStateTime();
    void setStateTime(double stateTime);
    void updateStateTime(double dt);
}
