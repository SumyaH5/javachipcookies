package com.c1games.terminal.starteralgo;

import com.c1games.terminal.algo.*;
import com.c1games.terminal.algo.io.GameLoop;
import com.c1games.terminal.algo.io.GameLoopDriver;
import com.c1games.terminal.algo.map.GameState;
import com.c1games.terminal.algo.map.MapBounds;
import com.c1games.terminal.algo.map.Unit;
import com.c1games.terminal.algo.units.UnitType;

import java.util.*;

public class Defences {
    
    private final int LAYOUTS = 1;

    // Excess points we have for deciding if we should upgrade
    // dummy values for now
    private final UPGRADE_SUPPORTS = 8;
    private final UPGRADE_TURRETS = 10;
    private final UPGRADE_LAYOUT = 20;

    private GameState state;

    private ArrayList<Coords> mainTurrets;
    private ArrayList<Coords> mainSupports;

    private ArrayList<Coords> wallLayout[LAYOUTS];
    private ArrayList<Coords> turretLayout[LAYOUTS];
    private int score[LAYOUTS];
    private int cost[LAYOUTS];

    private ArrayList<Coords> current;

    private int currentLayout;

    private int pts;

    private void initMain() {
        mainTurrets.add(new Coords(4, 12));
        mainTurrets.add(new Coords(23, 12));
        mainTurrets.add(new Coords(11, 12));
        mainTurrets.add(new Coords(16, 12));

        mainSupports.add(new Coords(13, 1))
        mainSupports.add(new Coords(14, 1))
    }

    private void initLayouts() {
        // STAGE 0 //
        // CENTER WALL FUNNEL //
        for (int i = 0; i < 7; i++) {
            wallLayout[0].add(Coords(i, 13));
            wallLayout[0].add(Coords(27 - i, 13));
        }

        score[0] = 30;
        cost[0] = 14;

        // STAGE 1 //
        // STAGE 0 + TURRETS // 
        for (int i = 0; i < 7; i++) {
            wallLayout[1].add(Coords(i, 13));
            wallLayout[1].add(Coords(27 - i, 13));
        }

        turretLayout[1].add(Coords(7, 13));
        turretLayout[1].add(Coords(20, 13))

        score[1] = 30;
        cost[1] = 18;

        // STAGE 2 //
        // LEFT WALLS // 
        for (int i = 0; i < 12; i++) {
            wallLayout[2].add(Coords(i, 13));
        }

        score[2] = 30;
        cost[2] = 12;

        // STAGE 3 //
        // RIGHT WALLS // 
        for (int i = 0; i < 12; i++) {
            wallLayout[3].add(Coords(27 - i, 13));
        }

        score[3] = 30;
        cost[3] = 12;
    }

    public Defences(GameState state) {
        this.state = state;

        initMain();
        initLayouts();
    }

    public void startTurn(int points) {
        this.pts = points;
        current.clear();

        deployMain();

        int best = 0;
        for (int i = 0; i < LAYOUTS; i++) {
            if (cost[best] > pts && cost[i] < cost[best]) {
                best = i;
                continue;
            }

            if (cost[i] > pts) {
                continue;
            }

            if (score[i] > score[best] || (score[i] == score[best] && cost[i] < cost[best])) {
                best = i;
            }
        }

        currentLayout = best;
        deployLayout(best);

        int upgrade = 0;    
        while (pts > UPGRADE_TURRETS && upgrade < mainTurrets.size()) {
            if (state.attemptSpawn(mainTurrets[upgrade], UnitType.Upgrade)) {
                //TODO
                pts -= 0;
            }
        } 

        upgrade = 0;
        while (pts > UPGRADE_SUPPORTS && upgrade < mainTurrets.size()) {
            if (state.attemptSpawn(mainSupports[upgrade], UnitType.Support)) {
                //TODO
                pts -= 0;
            }

            if (state.attemptSpawn(mainSupports[upgrade], UnitType.Upgrade)) {
                //TODO
                pts -= 0;
            }
        } 

        upgrade = 0;
        while (pts > UPGRADE_LAYOUT && upgrade < current.size()) {
            if (state.attemptSpawn(current[upgrade], UnitType.Upgrade)) {
                //TODO
                pts -= 0;
            }
        } 
    }

    public endTurn(int damage) {
        score[currentLayout] -= damage;
    }

    private void deployMain() {
        spawn(mainTurrets, UnitType.Turret, false);
    }

    private void deployLayout(int layout) {
        spawn(wallLayout[layout], UnitType.Wall, true);
        spawn(turretLayout[layout], UnitType.Turret, true);
    }  

    // public int getLayouts() {
    //     return LAYOUTS;
    // }

    private void spawn(ArrayList<Coords> spawns, UnitType unit, boolean curr) {
        for (Coords c : spawns) {
            boolean spawned = state.attemptSpawn(c, unit);

            if (spawned) {
                //TODO
                pts -= unit.cost1; // I don't know if this is the right variable, but I want to subtract the amt of points the unit costs
                                   //according to config, cost1 is for SP and cost2 is MP. Still not sure if this code will work. 
                if (curr) {
                    current.add()
                }
            }
        }
    }

    private void refund() {
        for (Coords c : current) {
            state.attemptRemoveStructure(c);;
        }
    }
}