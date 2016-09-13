///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2002, Eric D. Friedman All Rights Reserved.
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

import java.io.Serializable;

/**
 * Interface to support pluggable hashing strategies in maps and sets.
 * Implementors can use this interface to make the trove hashing
 * algorithms use an optimal strategy when computing hashcodes.
 *
 * Created: Sun Nov  4 08:56:06 2001
 *
 * @author Eric D. Friedman
 * @version $Id: TShortHashingStrategy.java,v 1.1 2004/11/09 15:48:47 ericdf Exp $
 */

public interface TShortHashingStrategy extends Serializable {
    /**
     * Computes a hash code for the specified short.  Implementors
     * can use the short's own value or a custom scheme designed to
     * minimize collisions for a known set of input.
     * 
     * @param short for which the hashcode is to be computed
     * @return the hashCode
     */
    public int computeHashCode(short val);
} // TShortHashingStrategy
