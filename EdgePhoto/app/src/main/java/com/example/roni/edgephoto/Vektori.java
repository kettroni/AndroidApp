package com.example.roni.edgephoto;

/**
 * Created by Roni on 5.1.2018.
 */

public class Vektori {

    private int[] arvot;

    public Vektori(int[] arvot) {
        this.arvot = arvot;
    }

    public int[] getArvot() {
        return this.arvot;
    }

    public int sisatulo(Vektori vektori) {
        int[] arvot = vektori.getArvot();
        return arvot[0]*this.arvot[0] +
                arvot[1]*this.arvot[1] +
                arvot[2]*this.arvot[2] +
                arvot[3]*this.arvot[3] +
                arvot[4]*this.arvot[4] +
                arvot[5]*this.arvot[5] +
                arvot[6]*this.arvot[6] +
                arvot[7]*this.arvot[7] +
                arvot[8]*this.arvot[8];
    }

}
