/*
 *  Java OTR library
 *  Copyright (C) 2008-2009  Ian Goldberg, Muhaimeen Ashraf, Andrew Chung,
 *                           Can Tang
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of version 2.1 of the GNU Lesser General
 *  Public License as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ca.uwaterloo.crysp.otr.crypt;

/**
 * This class computes session keys from the shared secret
 * generated by DH keypair.
 *
 * @author Can Tang <c24tang@gmail.com>
 */

import ca.uwaterloo.crysp.otr.InBuf;
import ca.uwaterloo.crysp.otr.OTRException;

public class DHSesskeys {
	public byte[] sendctr = new byte[16];
	public byte[] rcvctr = new byte[16];

	public AESKey sendenc;
	public AESKey rcvenc;

	public byte[] sendmackey = new byte[20];
	HMACKey sendmac;
	public int sendmacused;
	public byte[] rcvmackey = new byte[20];
	HMACKey rcvmac;
	public int rcvmacused;
	
	private Provider prov;
	
	public DHSesskeys(Provider prov){
		this.prov=prov;
	}

	/** Increment the top half of a counter block */
	public void incctr() {

		for (int i = 7; i >= 0; i--) {
			sendctr[i]++;
			if (sendctr[i] != 0)
				break;
		}
		return;
	}
	
	/**
	 * Compare two counter values (8 bytes each). Return 0 if ctr == ctr2, < 0
	 * if ctr < ctr2 (as unsigned 64-bit values), > 0 if ctr > ctr2.
	 */
	public int cmpctr(byte[] ctr2, boolean send) {
		if (send) {
			for (int i = 0; i < 8; i++) {
				int c = sendctr[i] - ctr2[i];
				if (c != 0)
					return c;
			}
			return 0;
		}
		for (int i = 0; i < 8; i++) {
			int c = rcvctr[i] - ctr2[i];
			if (c != 0)
				return c;
		}
		return 0;
	}

	public void computeSession(KeyPair our_dh, DHPublicKey their_pub)
			throws OTRException {

		DHKeyAgreement agreement = prov.getDHKeyAgreement();
		agreement.init(our_dh.getPrivateKey());
		byte[] secret = agreement.generateSecret(their_pub);
		int seclen = secret.length;
		byte[] gabdata = new byte[seclen + 5];
		gabdata[1] = (byte) ((seclen >> 24) & 0xff);
		gabdata[2] = (byte) ((seclen >> 16) & 0xff);
		gabdata[3] = (byte) ((seclen >> 8) & 0xff);
		gabdata[4] = (byte) (seclen & 0xff);
		System.arraycopy(secret, 0, gabdata, 5, seclen);

		byte sendbyte, rcvbyte;
		/* Are we the "high" or "low" end of the connection? */
		MPI ourint = MPI.readMPI(new InBuf(((DHPublicKey) our_dh.getPublicKey())
				.getY()));
		MPI theirint = MPI.readMPI(new InBuf(their_pub.getY()));
		if (prov.compareMPI(ourint, theirint) > 0) {
			sendbyte = 0x01;
			rcvbyte = 0x02; //2; 	this needs to be fixed
		} else {
			sendbyte = 0x02; //2;
			rcvbyte = 0x01;
		}

		/* Calculate the sending encryption key */
		gabdata[0] = sendbyte;
		byte[] res = prov.getSHA1().hash(gabdata);
		byte[] aesSeed = new byte[16];
		System.arraycopy(res, 0, aesSeed, 0, 16);
		sendenc = prov.getAESKey(aesSeed);

		/* Calculate the sending MAC key */
		sendmackey = prov.getSHA1().hash(aesSeed);
		sendmac = prov.getHMACKey(sendmackey);

		/* Calculate the receiving encryption key */
		gabdata[0] = rcvbyte;
		res = prov.getSHA1().hash(gabdata);
		aesSeed = new byte[16];
		System.arraycopy(res, 0, aesSeed, 0, 16);
		rcvenc = prov.getAESKey(aesSeed);

		/* Calculate the receiving MAC key */
		rcvmackey = prov.getSHA1().hash(aesSeed);
		rcvmac = prov.getHMACKey(rcvmackey);

	}

}
