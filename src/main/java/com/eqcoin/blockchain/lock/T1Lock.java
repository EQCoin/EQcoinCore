/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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
package com.eqcoin.blockchain.lock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.eqcoin.blockchain.lock.LockTool.LockType;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 10, 2020
 * @email 10509759@qq.com
 */
public class T1Lock extends EQCLock {
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		lockType = LockType.T1;
	}

	public T1Lock() {
		super();
	}
	
	public T1Lock(String readableLock) throws Exception {
		if (!LockTool.isReadableLockSanity(readableLock)) {
			throw new IllegalStateException("Readable lock isn't sanity: " + readableLock);
		}
		byte[] aiLock = LockTool.readableLockToAI(readableLock);
		lockType = LockType.get(aiLock[0]);
		if (lockType != LockType.T1) {
			throw new IllegalStateException("Invalid lock type expected T1 but actually: " + lockType);
		}
		lockCode = new byte[Util.SHA3_256_LEN];
		System.arraycopy(aiLock, 1, lockCode, 0, aiLock.length - 1);
	}

	public T1Lock(byte[] bytes) throws Exception {
		super(bytes);
	}

	public T1Lock(ByteArrayInputStream is) throws Exception {
		parse(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		lockCode = EQCType.parseNBytes(is, Util.SHA3_256_LEN);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBodyBytes()
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(lockCode);
		return os;
	}
	
	public Value getProofLength() {
		return  new Value(Util.T1_LOCK_PROOF_SPACE_COST);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.passport.Lock#isSanity()
	 */
	@Override
	public boolean isSanity() {
		return (lockType != null && lockType == LockType.T1 && lockCode != null && lockCode.length == Util.SHA3_256_LEN);
	}
	
	public String toInnerJson() {
		return "\"T1Lock\":" + "{\n" 
				+ "\"LockType\":" + lockType + ",\n"
				+ "\"PublickeyHash\":" + "\"" + Util.getHexString(lockCode) + "\""
				+ "\n" + "}";
	}
	
}
