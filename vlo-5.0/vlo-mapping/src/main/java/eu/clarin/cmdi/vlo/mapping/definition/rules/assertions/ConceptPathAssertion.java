/*
 * Copyright (C) 2022 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.mapping.definition.rules.assertions;

import com.google.common.base.Joiner;
import static com.google.common.base.Predicates.not;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Assertion on concept paths. Examples:
 *
 *
 * <pre>{@code
 * // exact match of one concept
 * new ConceptPathAssertion("http://concept1");
 *
 * // exact match of two concepts
 * new ConceptPathAssertion("http://concept1", "http://concept2");
 *
 * // exact match of first concept, any actuals after that
 * new ConceptPathAssertion("http://concept1", "*");
 *
 * // exact match of a concept, any actuals before that
 * new ConceptPathAssertion("*", "http://concept1");
 *
 * // match of two concepts with any actual between and after these
 * new ConceptPathAssertion("http://concept1", "*", "http://concept2", "*");
 * }</pre>
 *
 * Note that paths start with the lowest item in the tree!
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ConceptPathAssertion extends ContextAssertion {

    public static final String WILDCARD = "*";
    private final static Joiner pathJoiner = Joiner.on(' ');
    private final static Splitter pathSplitter = Splitter.on(Pattern.compile("[\\s]+"));

    @XmlTransient
    private List<String> targetPath;

    public ConceptPathAssertion() {
    }

    public ConceptPathAssertion(String... target) {
        setTargetPath(Arrays.asList(target));
    }

    @XmlElement(name = "conceptPath")
    public String getConceptPath() {
        return pathJoiner.join(targetPath);
    }

    public void setConceptPath(String path) {
        setTargetPath(pathSplitter.splitToList(path.trim()));
    }

    public final void setTargetPath(List<String> target) {
        this.targetPath = target;
    }

    public final void setTargetPath(Iterable<String> target) {
        this.targetPath = ImmutableList.copyOf(target);
    }

    public List<String> getTargetPath() {
        return targetPath;
    }

    @Override
    public Boolean evaluate(ValueContext context) {
        final Iterator<String> targetIterator = targetPath.iterator();
        final Iterator<String> actualIterator = context.getConceptPath().iterator();

        return evaluate(targetIterator, actualIterator);
    }

    private Boolean evaluate(final Iterator<String> target, final Iterator<String> actual) {
        // Iterate over target and compare actual
        while (target.hasNext()) {
            final String currentTarget = target.next();
            // Is current a wildcard?
            if (currentTarget.equals(WILDCARD)) {
                return evaluateWildcard(target, actual);
            } else {
                if (!nextActualMatches(actual, currentTarget)) {
                    // Mismatch here means immediate negative result
                    return false;
                }
            }
        }

        // Target has been exhausted; IFF actual has also been exhausted, we
        // have a match
        return !actual.hasNext();
    }

    /**
     * Checks if the next actual matches, moving actual to the next in the
     * process
     *
     * @param actual iterator, will be moved to next if available
     * @param currentTarget target to look for
     * @return whether next actual matches
     */
    private boolean nextActualMatches(final Iterator<String> actual, final String currentTarget) {
        if (actual.hasNext()) {
            final String nextActual = actual.next();
            // Compare target to actual
            return currentTarget.equals(nextActual);
        } else {
            // No next actual in context of next target, which means mismatch
            return false;
        }
    }

    /**
     * Process if current target is a wildcard
     *
     * @param target target iterator
     * @param actual actual iterator
     * @return
     */
    private Boolean evaluateWildcard(final Iterator<String> target, final Iterator<String> actual) {
        // Current item in 'target' is a wild card - first check if there are 
        // any targets beyond
        if (target.hasNext()) {
            // There is a next target, it needs to be matched somewhere in the
            // remainder of the actual
            final String currentTarget = target.next();
            while (actual.hasNext()) {
                final String currentActual = actual.next();
                if (currentTarget.equals(currentActual)) {
                    // Match found! we can evaluate the remainder of target
                    // and actual as a simple case
                    return evaluate(target, actual);
                } // else ignore and keep looking
            }
            // Exhausted actual without finding the next target!
            return false;
        } else {
            // No next target, so anything in actual is acceptable!
            return true;
        }
    }

}
