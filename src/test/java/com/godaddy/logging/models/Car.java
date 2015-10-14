/**
 *
 * Copyright (c) 2015 GoDaddy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.godaddy.logging.models;

import lombok.Getter;
import lombok.Setter;

public class Car {
    @Setter
    @Getter
    private String model;
    @Getter
    @Setter
    private Integer year;
    @Getter
    @Setter
    private String make;
    @Getter
    @Setter
    private double cost;
    @Getter
    @Setter
    private Country country;
    @Getter
    @Setter
    private Engine engine;

    public String test = "HI";

    public Car(String model, Integer year, String make, double cost, Country country, Engine engine) {
        this.model = model;
        this.year = year;
        this.make = make;
        this.cost = cost;
        this.country = country;
        this.engine = engine;
    }

    public String toString() {
        return "My car is a " + year + " " + make + " " + model + ". It cost me $" + cost + ". I bought it in " + country +
               ". It has " + (engine != null ? "a " + engine.getName() : "no") + " Engine.";
    }

}
