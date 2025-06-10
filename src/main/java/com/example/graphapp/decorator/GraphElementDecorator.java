package com.example.graphapp.decorator;

import javafx.scene.canvas.GraphicsContext;

public abstract class GraphElementDecorator implements GraphElementView {
    protected GraphElementView decoratedElementView;

    public GraphElementDecorator(GraphElementView decoratedElementView) {
        this.decoratedElementView = decoratedElementView;
    }

    @Override
    public void draw(GraphicsContext gc) {
        decoratedElementView.draw(gc); // Délègue le dessin à l'élément décoré
    }
    
    @Override
    public Object getModelElement() {
        return decoratedElementView.getModelElement();
    }

    @Override
    public boolean contains(double x, double y) {
        return decoratedElementView.contains(x,y);
    }
}