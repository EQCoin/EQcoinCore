/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
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
package com.eqcoin.blockchain.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.acl.Owner;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.lock.EQCLock;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.EQCPublickey;
import com.eqcoin.blockchain.lock.LockTool.LockType;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.rpc.TailInfo;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * Passport table's schema after refactor meet 3NF now //does not match 3NF but very blockchain.
 * @author Xun Wang
 * @date Nov 5, 2018
 * @email 10509759@qq.com
 */
public abstract class Passport extends EQCSerializable {
	/**
	 * Header field include PassportType
	 * Passport type can also used to represent different passport type's version
	 */
	private PassportType passportType;
	/**
	 * Body field include ID, LockID, balance, nonce and updateHeight
	 */
	private ID id;
	private ID lockID;
	private Value balance;
	private ID nonce;
	private ID updateHeight;
	
	/**
	 * PassportType include EQCOINSEED Passport and ASSET Passport.
	 * 
	 * @author Xun Wang
	 * @date May 19, 2019
	 * @email 10509759@qq.com
	 */
	public enum PassportType {
		EQCOINROOT, ASSET, SMARTCONTRACT;
		public static PassportType get(int ordinal) {
			PassportType passportTypeType = null;
			switch (ordinal) {
			case 0:
				passportTypeType = PassportType.EQCOINROOT;
				break;
			case 1:
				passportTypeType = PassportType.ASSET;
				break;
			case 2:
				passportTypeType = PassportType.SMARTCONTRACT;
				break;
			}
			return passportTypeType;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	public static PassportType parsepassportType(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		PassportType passportType = null;
		passportType = PassportType.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
		return passportType;
	}

	public static Passport parsePassport(byte[] bytes) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		Passport account = null;
		PassportType passportType = parsepassportType(is);

		try {
			if (passportType == PassportType.ASSET) {
				account = new AssetPassport(bytes);
			} 
			else if (passportType == passportType.EQCOINROOT) {
				account = new EQcoinRootPassport(bytes);
			} 
		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return account;
	}
	
//	public static Account parseAccount(O o) throws NoSuchFieldException, IllegalStateException, IOException {
//		return parseAccount(o.getProtocol().array());
//	}
	
	public static PassportType parsePassportType(EQCLock lock) {
		PassportType passportType = null;
		if(lock.getLockType() == LockType.T1 || lock.getLockType() == LockType.T2) {
			passportType = PassportType.ASSET;
		}
		return passportType;
	}
	
	public static Passport createAccount(EQCLock key) {
		Passport passport = null;
		PassportType passportTypeType = parsePassportType(key);

		try {
			if (passportTypeType == PassportType.ASSET) {
				passport = new AssetPassport();
//				passport.setKey(key);
			} else if (passportTypeType == passportTypeType.EQCOINROOT) {
				passport = null;
			} 
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return passport;
	}

	public Passport(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	@Override
	public void parseHeader(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
 		passportType = PassportType.get(EQCType.parseID(is).intValue());
	}
	
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException, Exception {
		// Parse PassportID
		id = EQCType.parseID(is);
		// Parse LockID
		lockID = EQCType.parseID(is);
		// Parse Balance
		balance = EQCType.parseValue(is);
		// Parse Nonce
		nonce = EQCType.parseID(is);
		// Parse Update Height
		updateHeight = EQCType.parseID(is);
	}
	
	protected void init() {
		balance = new Value();
		nonce = new ID();
		updateHeight = new ID();
	}
	
	public Passport(PassportType passportTypeType) {
		super();
		this.passportType = passportTypeType;
	}
	
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		getHeaderBytes(os);
		getBodyBytes(os);
		return os;
	}
	
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		os.write(passportType.getEQCBits());
		return os;
	}
	
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(id.getEQCBits());
		os.write(lockID.getEQCBits());
		os.write(balance.getEQCBits());
		os.write(nonce.getEQCBits());
		os.write(updateHeight.getEQCBits());
		return os;
	}
	
	public byte[] getHash() throws Exception {
//		if(hash == null) {
//			hash = Util.EQCCHA_MULTIPLE_DUAL(getHashBytes(soleUpdate), Util.HUNDREDPULS, true, false);
//		}
		return null;
	}
	
	/**
	 * @return the passportType
	 */
	public PassportType getpassportType() {
		return passportType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" +
				toInnerJson() +
				"\n}";
	}

	public String toInnerJson() {
		return 
					"\"PassportType\":" + "\"" + passportType + "\"" + ",\n" +
					"\"ID\":" + "\"" + id + "\"" + ",\n" +
					"\"LockID\":" + "\"" + lockID + "\"" + ",\n" +
					"\"Balance\":" + "\"" + balance + "\"" + ",\n" +
					"\"Nonce\":" + "\"" + nonce + "\"" + ",\n" +
					"\"UpdateHeight\":" + "\"" + updateHeight + "\"" + 
					"\n}";
	}
	
	/**
	 * Body field include ID, LockID, totalIncome, totalCost, balance, nonce
	 * @throws Exception 
	 */
	
	@Override
	public boolean isSanity() throws Exception {
		if(passportType == null || id == null || lockID == null || balance == null || nonce == null || updateHeight == null) {
			return false;
		}
		if(id.isSanity() || lockID.isSanity() || balance.isSanity() || nonce.isSanity() || updateHeight.isSanity()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	public O getO() throws Exception {
		return new O(ByteBuffer.wrap(getBytes()));
	}
	
	/**
	 * For system's security here need check if balance is enough for example SmartContract maybe provide wrong input amount
	 * @param Value
	 */
	public void withdraw(Value value) {
		EQCType.assertNotBigger(value, balance);
		balance = balance.subtract(value);
	}
	
	public void deposit(Value value) {
		EQCType.assertNotBigger(value, Util.MAX_EQcoin);
		balance = balance.add(value);
	}

	/**
	 * @return the id
	 */
	public ID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(ID id) {
		this.id = id;
	}

	/**
	 * @return the lockID
	 */
	public ID getLockID() {
		return lockID;
	}

	/**
	 * @param lockID the lockID to set
	 */
	public void setLockID(ID lockID) {
		this.lockID = lockID;
	}

	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
	}

//	/**
//	 * @param nonce the nonce to set
//	 */
//	public void setNonce(ID nonce) {
//		this.nonce = nonce;
//	}

	public void increaseNonce() {
		nonce = nonce.getNextID();
	}
	
	/**
	 * @return the balance
	 */
	public Value getBalance() {
		return balance;
	}

	/**
	 * @return the updateHeight
	 */
	public ID getUpdateHeight() {
		return updateHeight;
	}

	/**
	 * @param updateHeight the updateHeight to set
	 */
	public void setUpdateHeight(ID updateHeight) {
		this.updateHeight = updateHeight;
	}
	
}
