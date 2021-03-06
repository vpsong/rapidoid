package org.rapidoid.writable;

	/*
	 * #%L
 * rapidoid-commons
 * %%
 * Copyright (C) 2014 - 2018 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
	 */

import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.u.U;

import java.io.OutputStream;
import java.util.Arrays;

@Authors("Nikolche Mihajlovski")
@Since("5.3.4")
public class ReusableWritable extends OutputStream implements Writable {

	private static final int MAX_SIZE = 128 * 1024 * 1024; // max 128 MB

	private byte[] bytes;

	private int count;

	public ReusableWritable() {
		this(128); // 128 B as default size
	}

	public ReusableWritable(int size) {
		U.must(size >= 0);
		bytes = new byte[size];
	}

	public int capacity() {
		return bytes.length;
	}

	private void requireCapacity(int requiredCapacity) {
		if (requiredCapacity > capacity()) {
			expand(requiredCapacity);
		}
	}

	private void expand(int requiredCapacity) {

		// double the capacity, using "long" to prevent overflow
		long newCapacity = capacity() * 2;

		// if still not enough, expand up to the required capacity
		if (newCapacity < requiredCapacity) newCapacity = requiredCapacity;

		// if the size is too big, cap it
		newCapacity = Math.min(newCapacity, MAX_SIZE);

		// after all, the new capacity might not be enough
		U.must(newCapacity >= requiredCapacity, "Cannot expand the buffer to size %s, the limit is %s!", requiredCapacity, newCapacity);

		bytes = Arrays.copyOf(bytes, (int) newCapacity);
	}

	public void reset() {
		count = 0;
	}

	public int size() {
		return count;
	}

	public byte[] array() {
		return bytes;
	}

	public byte[] copy() {
		return Arrays.copyOf(bytes, count);
	}

	@Override
	public void writeByte(byte byteValue) {
		requireCapacity(count + 1);

		bytes[count] = byteValue;
		count++;
	}

	@Override
	public void writeBytes(byte[] src) {
		writeBytes(src, 0, src.length);
	}

	@Override
	public void writeBytes(byte[] src, int offset, int length) {
		if ((offset < 0) || (offset > src.length) || (length < 0) || (offset + length - src.length > 0)) {
			throw new IndexOutOfBoundsException();
		}

		requireCapacity(count + length);
		System.arraycopy(src, offset, bytes, count, length);
		count += length;
	}

	// OutputStream methods:

	@Override
	public void write(int src) {
		writeByte((byte) src);
	}

	@Override
	public void write(byte[] src, int off, int len) {
		writeBytes(src, off, len);
	}

	@Override
	public void write(byte[] src) {
		writeBytes(src);
	}

}
