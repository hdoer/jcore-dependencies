///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package de.julielab.gnu.trove;

/**
 * Iterator for float collections.
 *
 * @author Eric D. Friedman
 * @version $Id: TFloatIterator.java,v 1.3 2004/11/09 15:48:46 ericdf Exp $
 */

public class TFloatIterator extends TPrimitiveIterator {
    /** the collection on which the iterator operates */
    private final TFloatHash _hash;

    /**
     * Creates a TFloatIterator for the elements in the specified collection.
     */
    public TFloatIterator(TFloatHash hash) {
	super(hash);
	this._hash = hash;
    }

    /**
     * Advances the iterator to the next element in the underlying collection
     * and returns it.
     *
     * @return the next float in the collection
     * @exception NoSuchElementException if the iterator is already exhausted
     */
    public float next() {
	moveToNextIndex();
	return _hash._set[_index];
    }
}// TFloatIterator
