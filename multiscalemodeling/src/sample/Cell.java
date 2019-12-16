package sample;

import javafx.scene.paint.Color;

public class Cell implements Cloneable {

    private boolean alive;
    private int grainType;
    private Color typeColor;
    private int energy;
    private boolean isRecrystallized;

    public boolean isRecrystallized() {
        return isRecrystallized;
    }

    public void setRecrystallized(boolean recrystallized) {
        isRecrystallized = recrystallized;
    }

    public Cell(boolean alive, int grainType, Color typeColor, int energy, boolean isRecrystallized) {
        this.setAlive(alive);
        this.setGrainType(grainType);
        this.setTypeColor(typeColor);
        this.setEnergy(energy);
        this.setRecrystallized(isRecrystallized);
    }


    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getGrainType() {
        return grainType;
    }

    public void setGrainType(int grainType) {
        this.grainType = grainType;
    }

    public Color getTypeColor() {
        return typeColor;
    }

    public void setTypeColor(Color typeColor) {
        this.typeColor = typeColor;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    @Override
    public Cell clone() {
        return new Cell(alive, grainType, typeColor, energy, isRecrystallized);
    }

    public void changeState(int neighboursCount, int type, Color color, int energy, boolean isRecrystallized) {

        if (!alive) {
            if(neighboursCount>0){
                alive=true;
                grainType =type;
                typeColor = color;
                this.energy = energy;
                this.isRecrystallized = isRecrystallized;
            }

        }

    }
}