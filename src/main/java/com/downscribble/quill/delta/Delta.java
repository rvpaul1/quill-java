package com.downscribble.quill.delta;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Delta {

	private List<Op> ops;

	public Delta() {
		this.ops = new ArrayList<>();
	}

	public Delta(List<Op> ops) {
		this.ops = ops;
	}

	public Delta(Delta other) {
		this.ops = other.getOps();
	}

	public Delta insert(Object arg) {
		return this.insert(arg, null);
	}

	public Delta insert(Object arg, AttributeMap attributes) {
		final Op newOp = new Op();
		if (arg instanceof String && ((String) arg).length() == 0) {
			return this;
		}
		newOp.setInsert(arg);
		if (attributes != null && attributes.keySet().size() > 0) {
			newOp.setAttributes(attributes);
		}
		return this.push(newOp);
	}

	public Delta delete(int length) {
		if (length <= 0) {
			return this;
		}
		final Op newOp = new Op();
		newOp.setDelete(length);
		return this.push(newOp);
	}

	public Delta retain(int length) {
		return this.retain(length, null);
	}

	public Delta retain(int length, AttributeMap attributes) {
		if (length <= 0) {
			return this;
		}
		final Op newOp = new Op();
		newOp.setRetain(length);
		if (attributes != null && attributes.keySet().size() > 0) {
			newOp.setAttributes(attributes);
		}
		return this.push(newOp);
	}

	public Delta chop() {
		if (this.ops.size() > 0 && this.ops.get(this.ops.size() - 1).getRetain() != null
				&& this.ops.get(this.ops.size() - 1).getAttributes() == null) {
			this.ops.remove(this.ops.size() - 1);
		}
		return this;
	}

	public Delta compose(Delta other) {

		if (other == null || other.getOps() == null || other.getOps().size() == 0) {
			return this;
		}

		final Iterator thisIter = Op.iterator(this.getOps());
		final Iterator otherIter = Op.iterator(other.getOps());
		final List<Op> ops = new ArrayList<>();
		final Op firstOther = otherIter.peek();

		if (firstOther != null && firstOther.getRetain() != null
				&& firstOther.getAttributes() == null) {
			int firstLeft = firstOther.getRetain();

			while (thisIter.peekType().equals(Op.INSERT) && thisIter.peekLength() <= firstLeft) {
				firstLeft -= thisIter.peekLength();
				ops.add(thisIter.next());
			}
			if (firstOther.getRetain() - firstLeft > 0) {
				otherIter.next(firstOther.getRetain() - firstLeft);
			}
		}

		final Delta delta = new Delta(ops);

		while (thisIter.hasNext() || otherIter.hasNext()) {
			if (otherIter.peekType().equals(Op.INSERT)) {
				delta.push(otherIter.next());
			} else if (thisIter.peekType().equals(Op.DELETE)) {
				delta.push(thisIter.next());
			} else {
				final int length = Math.min(thisIter.peekLength(), otherIter.peekLength());
				final Op thisOp = thisIter.next(length);
				final Op otherOp = otherIter.next(length);
				if (otherOp.getRetain() != null) {
					final Op newOp = new Op();
					if (thisOp.getRetain() != null) {
						newOp.setRetain(length);
					} else {
						newOp.setInsert(thisOp.getInsert());
					}
					// Preserve null when composing with a retain, otherwise remove it for inserts
					final AttributeMap attributes = AttributeMap.compose(thisOp.getAttributes(),
							otherOp.getAttributes(), thisOp.getRetain() != null);
					if (attributes != null) {
						newOp.setAttributes(attributes);
					}
					delta.push(newOp);

					// Optimization if rest of other is just retain
					if (!otherIter.hasNext() && delta.getOps().get(delta.getOps().size() - 1).equals(newOp)) {
						final Delta rest = new Delta(thisIter.rest());
						return delta.concat(rest).chop();
					}

					// Other op should be delete, we could be an insert or retain
					// Insert + delete cancels out
				} else if (otherOp.getDelete() != null && thisOp.getRetain() != null) {
					delta.push(otherOp);
				}
			}
		}
		return delta.chop();
	}

	public Delta concat(Delta other) {
		final Delta delta = new Delta(this.ops.subList(0, this.ops.size()));
		if (other.getOps() != null && other.getOps().size() > 0) {
			delta.push(other.getOps().get(0));
			delta.getOps().addAll(other.getOps().subList(1, other.getOps().size()));
		}
		return delta;
	}

	public Delta push(Op newOp) {
		int index = this.ops.size();
		Op lastOp = index > 0 ? this.ops.get(index - 1) : null;
		// TODO investigate whether this needs to be a deep copy
//	    newOp = cloneDeep(newOp);
		if (lastOp != null) {
			if (newOp.getDelete() != null && lastOp.getDelete() != null) {
				lastOp.setDelete(lastOp.getDelete() + newOp.getDelete());
				return this;
			}
			// Since it does not matter if we insert before or after deleting at the same
			// index,
			// always prefer to insert first
			if (lastOp.getDelete() != null && newOp.getInsert() != null) {
				index -= 1;
				lastOp = index > 0 ? this.ops.get(index - 1) : null;
				if (lastOp == null) {
					this.ops.add(0, newOp);
					return this;
				}
			}
			if (newOp.getAttributes() == lastOp.getAttributes()
					|| (newOp.getAttributes() != null && newOp.getAttributes().equals(lastOp.getAttributes()))) {
				if (newOp.getInsert() != null && lastOp.getInsert() != null
						&& newOp.getInsert() instanceof String 
						&& lastOp.getInsert() instanceof String) {
					lastOp.setInsert((String) lastOp.getInsert() + newOp.getInsert());
					if (newOp.getAttributes() != null) {
						lastOp.setAttributes(newOp.getAttributes());
					}
					return this;
				} else if (newOp.getRetain() != null && lastOp.getRetain() != null) {
					lastOp.setRetain(lastOp.getRetain() + newOp.getRetain());
					if (newOp.getAttributes() != null) {
						lastOp.setAttributes(newOp.getAttributes());
					}
					return this;
				}
			}
		}
		if (index == this.ops.size()) {
			this.ops.add(newOp);
		} else {
			this.ops.add(index, newOp);
		}
		return this;
	}

}
