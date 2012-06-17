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

import java.util.List;

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
import penran.zombies.ui.Level.Road;
import penran.zombies.ui.Level.Town;

public class Land extends Pane {

  private final Rectangle background;

  private final Group items;

  private final int marginWidth;

  private final int marginHeight;

  private final Font font;

  public Land(Level level, int width, int height, int marginWidth,
      int marginHeight) {

    this.marginWidth = marginWidth;
    this.marginHeight = marginHeight;

    setPrefSize(width, height);

    font = Font.font("arial", 20);
    final Text text = new Text(0, 0, "Item: ");
    text.setFill(Color.WHITE);
    text.setFont(font);
    text.setY(font.getSize());

    Group towns = new Group();
    Group halo = new Group();
    for (final Town t : level.towns) {
      if (t.control)
        continue;
      double radius = t.size / 2d;

      int ift = (int) Math.round(255*(t.size - t.infected)/(double) t.size);
      Circle c = new Circle(t.longitude, t.latitude, radius, Color.rgb(255, ift, ift));
      c.setOnMouseClicked(new EventHandler<Event>() {
        @Override
        public void handle(Event paramT) {
          text.setText("Item: " + t.name);
        }
      });

      towns.getChildren().add(c);

      Circle bound = new Circle(t.longitude, t.latitude, radius + 1);
      bound.setStrokeType(StrokeType.OUTSIDE);
      bound.setStroke(Color.web("white", 1));
      bound.setStrokeWidth(2f);
      halo.getChildren().add(bound);
    }
    towns.setEffect(new BoxBlur(2, 2, 2));
    halo.setEffect(new BoxBlur(2, 2, 2));

    Group roads = new Group();
    for (final Road r : level.roads) {
      List<Town> ep = r.endPoints;
      List<Town> ct = r.control;

      double[] points = new double[ep.size() * 2 + ct.size() * 2];
      points[0] = ep.get(0).longitude;
      points[1] = ep.get(0).latitude;
      for (int i = 0; i < ct.size(); i++) {
        points[i * 2 + 2] = ct.get(i).longitude;
        points[i * 2 + 3] = ct.get(i).latitude;
      }
      points[ct.size() * 2 + 2] = ep.get(1).longitude;
      points[ct.size() * 2 + 3] = ep.get(1).latitude;

      Polyline l = new Polyline(points);
      l.setOnMouseClicked(new EventHandler<Event>() {
        @Override
        public void handle(Event paramT) {
          text.setText("Item: " + r.name);
        }
      });

      l.setStrokeType(StrokeType.OUTSIDE);
      l.setStroke(Color.web("white", 0.8f));
      l.setStrokeWidth(1f);
      roads.getChildren().add(l);
    }

    background = new Rectangle(0, 0, width, height);
    background.setFill(Color.BLACK);
    items = new Group(halo, roads, towns);
    items.setManaged(false);

    background.setHeight(height);
    background.setWidth(width);

    Bounds bounds = items.getBoundsInParent();
    double scale = Math.min((width - marginWidth) / bounds.getWidth(), (height
        - marginHeight - font.getSize())
        / bounds.getHeight());
    items.setScaleX(scale);
    items.setScaleY(scale);
    items.setTranslateX(-items.getBoundsInParent().getMinX() + marginWidth / 2d - items.getTranslateX());
    items.setTranslateY(-items.getBoundsInParent().getMinY() + font.getSize()
        + marginWidth / 2d - items.getTranslateY());

    getChildren().add(new Group(background, items, text));
  }

}
