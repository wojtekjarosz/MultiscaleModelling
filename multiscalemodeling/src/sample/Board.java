package sample;

import javafx.scene.paint.Color;

import java.util.*;

public class Board {

    private Cell[][] cells;
    private int width;
    private int height;
    private boolean period;
    private int probability;
    private String neighbourhoodType;
    private List<Integer> selectedGrains;
    Random rand;



    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        cells = new Cell[width][height];
        rand = new Random();
        selectedGrains = new ArrayList<>();
        resetAll();
    }

    // reset calej planszy
    private void resetAll() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j] = new Cell(false, 0, null);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // ustawienie wartosci komorki
    public void setCellValue(int x, int y, boolean isAlive) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            cells[x][y].setAlive(isAlive);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    //ustawianie typu ziarna
    public void setCellGrainType(int x, int y, int grainType) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            cells[x][y].setGrainType(grainType);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public void setCellColor(int x, int y, Color typeColor) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            cells[x][y].setTypeColor(typeColor);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    // pobranie wartosci komorki
    public boolean getCellValue(int x, int y) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            return cells[x][y].isAlive();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    // pobranie typu ziarna
    public int getCellGrainType(int x, int y) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            return cells[x][y].getGrainType();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public Color getCellColor(int x, int y) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            return cells[x][y].getTypeColor();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean isPeriod() {
        return period;
    }

    public void setNeighbourhoodType(String neighbourhoodType) {
        this.neighbourhoodType = neighbourhoodType;
    }

    public void setPeriod(boolean period) {
        this.period = period;
    }

    public List<Integer> getSelectedGrains() {
        return selectedGrains;
    }

    public void setSelectedGrains(List<Integer> selectedGrains) {
        this.selectedGrains = selectedGrains;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    // wykonanie aktualnej tury
    public boolean nextCycle() {
        int[] cellInfo = null;
        // kopiowanie aktualnego stanu
        Cell[][] newBoard = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newBoard[i][j] = cells[i][j].clone();
            }
        }

        // wykonanie akcji dla danej komorki
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(neighbourhoodType=="von Neumann'a")
                    cellInfo = getGrainsGrowthType(i, j);
                else if(neighbourhoodType=="Moore'a")
                    cellInfo = getMooreGrainsGrowthType(i, j);
                newBoard[i][j].changeState(cellInfo[0],cellInfo[1], cells[i][j].getTypeColor());
            }
        }

        cells = newBoard;

        return isFinished();
    }

    public boolean isFinished() {
        boolean value = true;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(!cells[i][j].isAlive()){
                    value = false;
                }
            }
        }
        return value;
    }

    public double getProgress(){
        double max = width*height;
        int counter = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(cells[i][j].isAlive()){
                    counter++;
                }
            }
        }
        return counter/max;

    }

    // wybranie typu rozrostu
    public int[] getGrainsGrowthType(int i, int j) {
        int [] info = new int[2];
        Map<Integer,Integer> neighbours = new HashMap<>();

        int type=0;


            if (!period) {
                int left = Math.max(i - 1, 0);
                int up = Math.max(j - 1, 0);
                int right = Math.min(i + 1, width - 1);
                int down = Math.min(j + 1, height - 1);
                int[] x = {left, i, right, i};
                int[] y = {j, up, j, down};
                for (int k = 0; k < 4; k++) {

                    type = cells[x[k]][y[k]].getGrainType();
                    getNeighboursInfo(neighbours, type);
                }
            } else {
                //periodycznie
                int left = i - 1;
                int up = j - 1;
                int right = i + 1;
                int down = j + 1;
                int[] x = {left, i, right, i};
                int[] y = {j, up, j, down};
                int tmpX, tmpY;
                for (int k = 0; k < 4; k++) {
                    tmpX = x[k];
                    tmpY = y[k];
                    if (x[k] == -1) tmpX = width - 1;
                    if (x[k] == width) tmpX = 0;
                    if (y[k] == -1) tmpY = height - 1;
                    if (y[k] == height) tmpY = 0;

                    type = cells[tmpX][tmpY].getGrainType();
                    getNeighboursInfo(neighbours, type);
                }
            }

        //max
        if(neighbours.isEmpty()) {
            info[1] = 0;
            info[0] = 0;
        }else {
            info[1] = neighbours.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
            info[0] = neighbours.get(info[1]);
        }

        return info;
    }

    public void getNeighboursInfo(Map<Integer, Integer> neighbours, int type){
        if (neighbours.containsKey(type)) {
            int count = neighbours.get(type);
            neighbours.put(type, count + 1);
        } else if (type != 0 && type != -1 && !selectedGrains.contains(type)) {
            neighbours.put(type, 1);
        }
    }

    public int[] getMooreGrainsGrowthType(int i, int j) {
        int [] info = new int[2];
        int [] maxInfo = new int[2];
        Map<Integer,Integer> neighbours = new HashMap<>();

        int type=0;

        //periodycznie
        int startX = i - 1;
        int startY = j - 1;
        int endX = i + 1;
        int endY = j + 1;


        for(int k =0; k<4; k++){
            int counter = 0;
            int tmpX, tmpY;
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    if(k == 0 || (k == 1 && counter % 2 == 1) || (k == 2 && counter % 2 == 0 && counter!=4)) {
                        tmpX = x;
                        tmpY = y;
                        if (x == -1) tmpX = width - 1;
                        if (x == width) tmpX = 0;
                        if (y == -1) tmpY = height - 1;
                        if (y == height) tmpY = 0;


                        type = cells[tmpX][tmpY].getGrainType();
                        getMooreNeighboursInfo(neighbours, type);
                    }
                    counter++;
                }
            }


            //max
            if (neighbours.isEmpty()) {
                info[1] = 0;
                info[0] = 0;
            } else {
                info[1] = neighbours.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
                info[0] = neighbours.get(info[1]);
                if(k == 0){
                    maxInfo[0] = info[0];
                    maxInfo[1] = info[1];
                }
            }

            if ((k==0 && info[0] >= 5) || (k==1 && info[0] >=3) || (k==2 && info[0] >=3)) {
                return info;
            }else if(k==3){
                if(rand.nextInt(101) < probability){
                    return maxInfo;
                }else{
                    info[0] =0;
                    info[1] = 0;
                    return info;
                }

            }
            neighbours.clear();

        }

        return info;
    }

    public void getMooreNeighboursInfo(Map<Integer, Integer> neighbours, int type){
        if (neighbours.containsKey(type)) {
            int count = neighbours.get(type);
            neighbours.put(type, count + 1);
        } else if (type != 0 && type != -1 && !selectedGrains.contains(type)) {
            neighbours.put(type, 1);
        }
    }

    public boolean isCellOnBorder(int i, int j){
        boolean isOnBorder = false;
        int type=0;
        int currentType = cells[i][j].getGrainType();
        if (!period) {

            int left = Math.max(i - 1, 0);
            int up = Math.max(j - 1, 0);
            int right = Math.min(i + 1, width - 1);
            int down = Math.min(j + 1, height - 1);
            int[] x= {left,i,right,i};
            int[] y= {j, up, j, down};
            for (int k = 0; k < 4; k++) {

                type = cells[x[k]][y[k]].getGrainType();
                if(type != currentType)
                    isOnBorder = true;
            }
        } else {
            //periodycznie
            int left = i - 1;
            int up = j - 1;
            int right = i + 1;
            int down = j + 1;
            int[] x= {left,i,right,i};
            int[] y= {j, up, j, down};
            int tmpX, tmpY;
            for (int k = 0; k < 4; k++) {
                tmpX = x[k];
                tmpY = y[k];
                if (x[k] == -1) tmpX = width - 1;
                if (x[k] == width) tmpX = 0;
                if (y[k] == -1) tmpY = height - 1;
                if (y[k] == height) tmpY = 0;

                type = cells[tmpX][tmpY].getGrainType();
                if(type != currentType)
                    isOnBorder = true;
            }
        }

        return isOnBorder;
    }
}
