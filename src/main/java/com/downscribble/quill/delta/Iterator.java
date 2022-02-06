package com.downscribble.quill.delta;

import java.util.ArrayList;
import java.util.List;

public class Iterator {

	private List<Op> ops;
	private int index;
	private int offset;

	public Iterator(List<Op> ops) {
		this.ops = ops;
		// index and offset will default to 0
	}

	public boolean hasNext() {
		return this.peekLength() < Integer.MAX_VALUE;
	}

	public Op next() {
		return this.next(Integer.MAX_VALUE);
	}

	public Op next(int length) {

		final Op nextOp = this.index <= this.ops.size() - 1 ? this.ops.get(index) : null;

		if (nextOp != null) {
			final int opLength = Op.length(nextOp);
			final int offset = this.offset;
			if (length >= opLength - offset) {
				length = opLength - offset;
				this.index += 1;
				this.offset = 0;
			} else {
				this.offset += length;
			}
			final Op retOp = new Op();
			if (nextOp.getDelete() != null) {
				retOp.setDelete(length);
				return retOp;
			} else {
				if (nextOp.getAttributes() != null) {
					retOp.setAttributes(nextOp.getAttributes());
				}

				if (nextOp.getRetain() != null) {
					retOp.setRetain(length);
				} else if (nextOp.getInsert() != null && nextOp.getInsert() instanceof String) { // There's a bug here
																									// for embed
																									// inserts.
					retOp.setInsert(((String) nextOp.getInsert()).substring(offset, length + offset));
				} else {
					// Unreachable code. This branch is intended for embed inserts.
					retOp.setInsert(nextOp.getInsert());
				}
				return retOp;
			}
		} else {
			final Op retOp = new Op();
			retOp.setRetain(Integer.MAX_VALUE);
			return retOp;
		}

	}

	public int peekLength() {
		if (this.index <= this.ops.size() - 1) {
			return Op.length(this.ops.get(this.index)) - this.offset;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	public Op peek() {
		if (this.index > this.ops.size() - 1) {
			return null;
		}

		return this.ops.get(index);
	}

	public String peekType() {
		if (this.index <= this.ops.size() - 1) {
			if (this.ops.get(this.index).getDelete() != null) {
				return Op.DELETE;
			} else if (this.ops.get(this.index).getRetain() != null) {
				return Op.RETAIN;
			} else {
				return Op.INSERT;
			}
		}
		return Op.RETAIN;
	}

	public List<Op> rest() {
		// TODO Auto-generated method stub
		if (!this.hasNext()) {
			return new ArrayList<>();
		} else if (this.offset == 0) {
			return this.ops.subList(this.index, this.ops.size());
		} else {
			final int offset = this.offset;
			final int index = this.index;
			final Op next = this.next();
			final List<Op> rest = this.ops.subList(this.index, this.ops.size());
			this.offset = offset;
			this.index = index;
			rest.add(0, next);
			return rest;
		}
	}
}
