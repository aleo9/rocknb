/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocknb;

public interface Updater {
        void connected(int players, int id);
        void newRound();
}
