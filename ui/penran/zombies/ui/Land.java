/*
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package penran.zombies.ui;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import penran.zombies.ui.Level.Town;

public class Land extends Pane {

  private final Rectangle background;

  public Land(Level level, int width, int height, int marginWidth, int marginHeight) {
    setWidth(width);
    setHeight(height);

    Font font = Font.font("arial", 20);
    final Text text = new Text(0, 0, "Item: ");
    text.setFill(Color.WHITE);
    text.setFont(font);
    text.setY(font.getSize());

    Group towns = new Group();
    Group halo = new Group();
    for (final Town t : level.towns) {
      double radius = t.size / 2d;

      Circle c = new Circle(t.longitude, t.latitude, radius, Color.web("pink", 0.5));
      c.setOnMouseClicked(new EventHandler<Event>() {
        @Override
        public void handle(Event paramT) {
          text.setText("Item: " + t.name);
        }
      });

      towns.getChildren().add(c);

      Circle bound = new Circle(t.longitude, t.latitude, radius);
      bound.setStrokeType(StrokeType.OUTSIDE);
      bound.setStroke(Color.web("white", 0.8f));
      bound.setStrokeWidth(2f);
      halo.getChildren().add(bound);
    }
//    towns.setEffect(new BoxBlur(30, 30, 3));
    towns.setEffect(new BoxBlur(2, 2, 2));
    halo.setEffect(new BoxBlur(6, 6, 6));
//    towns.setEffect(new BoxBlur(10, 10, 3));

    Group items = new Group(halo, towns);
    items.setManaged(false);

    Bounds bounds = items.getBoundsInParent();
    double scale = Math.max((width - marginWidth) / bounds.getWidth(),
                            (height - marginHeight - font.getSize()) / bounds.getHeight());
    items.setScaleX(scale);
    items.setScaleY(scale);
    items.setTranslateX(-items.getBoundsInParent().getMinX());
    items.setTranslateY(-items.getBoundsInParent().getMinY() + font.getSize());

    background = new Rectangle(0, 0, width, height);
    background.setFill(Color.BLACK);

    getChildren().add(new Group(background, items, text));
  }

  @Override
  protected void layoutChildren() {
//    super.layoutChildren();

    double width = getWidth();
    double height = getHeight();
    background.setWidth(width);
    background.setHeight(height);

//    double top = getInsets().getTop();
//    double right = getInsets().getRight();
//    double left = getInsets().getLeft();
//    double bottom = getInsets().getBottom();
//
//    for (Node child :  getManagedChildren()) {
//      layoutInArea(child, left, top, width - left - right, height - top
//          - bottom, 0, Insets.EMPTY, true, true, HPos.CENTER, VPos.CENTER);
//    }
  }
}
