package com.heychinaski.historyhack.displayobjects;


public interface StatefulBlob extends Blob {

    /**
     * @return true if this StatefulBlob will draw
     *  it's final state at the next call to 
     *  drawAndIncrementState()
     */
    boolean willDrawFinalState();

}
