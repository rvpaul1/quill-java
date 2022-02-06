package com.downscribble.quill;

import lombok.Data;

import java.util.List;

@Data
public class Op {
	
	public static final String INSERT = "insert";
	public static final String DELETE = "delete";
	public static final String RETAIN ="retain";

	// only one property out of {insert, delete, retain} will be present
	private Object insert;
	private Integer delete;
	private Integer retain;
	
	private AttributeMap attributes;
	
	public static Iterator iterator(List<Op> ops) {
		return new Iterator(ops);
	}
	
	public static int length(Op op) {
		
		if (op == null) {			
			throw new IllegalArgumentException("Op must not be null!");
		}
		
		if (op.getInsert() != null) {
			if (op.getInsert() instanceof String) {				
				return ((String) op.getInsert()).length();
			} else {
				// Length of embed should be 1.
				return 1;
			}
		} else if (op.getDelete() != null) {
			return op.getDelete();
		} else if (op.getRetain() != null) {
			return op.getRetain();
		} else {
			throw new IllegalArgumentException("Exactly one of {insert, delete, retain} should be present.");
		}
	}
	
}
