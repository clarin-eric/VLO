/*
 * Copyright (C) 2018 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.wicket.model;

/**
 * Rating levels for measuring user satisfaction.
 *
 * Implementation of {@link https://github.com/clarin-eric/VLO/issues/165}.
 *
 * @see RatingPanel
 * @author Twan Goosen <twan@clarin.eu>
 */
public enum RatingLevel {

    VERY_DISSATISFIED("0", "sentiment_very_dissatisfied", "Very dissatisfied"),
    DISSATISFIED("1", "sentiment_dissatisfied", "Dissatisfied"),
    NEUTRAL("2", "sentiment_neutral", "Neutral"),
    SATISFIED("3", "sentiment_satisfied", "Satisfied"),
    VERY_SATISFIED("4", "sentiment_very_satisfied", "Very satisfied");

    private final String value;
    private final String icon;
    private final String description;

    private RatingLevel(String value, String icon, String description) {
        this.value = value;
        this.icon = icon;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

}
