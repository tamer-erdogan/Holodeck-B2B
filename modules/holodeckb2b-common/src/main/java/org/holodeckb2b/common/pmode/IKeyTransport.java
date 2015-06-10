/*
 * Copyright (C) 2015 The Holodeck B2B Team
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
package org.holodeckb2b.common.pmode;

/**
 * Defines the KeyTransport parameters within the encryption
 * configuration of a P-Mode.
 * 
 * @author Bram Bakx <bram at holodeck-b2b.org>
 */
public interface IKeyTransport {
    
    /**
     * Gets the key transport algorithm.
     * 
     * @return The key transport algorithm to be used.
     */
    public String getAlgorithm();
    
    /**
     * Gets the MGF algorithm.
     * 
     * @return The MGF algorithm to be used.
     */
    public String getMGFAlgorithm();
    
    /**
     * Gets the digest algorithm.
     * 
     * @return The digest algorithm to be used.
     */
    public String getDigestAlgorithm();
    
}
