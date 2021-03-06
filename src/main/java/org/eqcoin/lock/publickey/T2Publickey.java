/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 *
 * @copyright 2018-present EQcoin Planet All rights reserved...
 * Copyright of all works released by EQcoin Planet or jointly released by
 * EQcoin Planet with cooperative partners are owned by EQcoin Planet
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Planet reserves all rights to take 
 * any legal action and pursue any right or remedy available under applicable
 * law.
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
package org.eqcoin.lock.publickey;

import java.io.ByteArrayInputStream;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 10, 2020
 * @email 10509759@qq.com
 */
public class T2Publickey extends Publickey {
	
	public T2Publickey() {
	}

	public T2Publickey(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	public void parse(ByteArrayInputStream is) throws Exception {
		// Parse publicKey
		publickey = EQCCastle.parseNBytes(is, Util.P521_PUBLICKEY_LEN);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.lock.EQCPublickey#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(publickey == null) {
			Log.Error("publickey == null");
			return false;
		}
		if(publickey.length == Util.P521_PUBLICKEY_LEN) {
			Log.Error("publickey.length == Util.P521_PUBLICKEY_LEN");
			return false;
		}
		return true;
	}
	
	public String toInnerJson() {
		return "\"T2Publickey\":" + "\"" + Util.bytesTo512HexString(publickey) + "\"";
	}
	
}
