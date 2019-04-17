package com.ebla.viewone.services;
import java.util.HashMap;
import java.util.Map;

public enum AnnotationsPermissionsEnum {
	
	/** The correspondence status draft. */
	ReadOnlyAnnotationPermission("Read", 1),

	/** The correspondence status progress. */
	ReadWriteAnnotationPermission("Read/Write", 2);

	/** annotationsPermission. */
	public final String annotationsPermission;


	/** The value. */
	public final int value;

	/** The Constant map. */
	private static final Map<Integer, AnnotationsPermissionsEnum> map;
	static
	{
		map = new HashMap<Integer, AnnotationsPermissionsEnum>();
		for(AnnotationsPermissionsEnum v : AnnotationsPermissionsEnum.values())
		{
			map.put(v.value, v);
		}
	}

	/**
	 * Find by key.
	 *
	 * @param i the i
	 * @return the correspondence status enum
	 */
	public static AnnotationsPermissionsEnum findByKey(int i)
	{
		return map.get(i);
	}

	/**
	 * Instantiates a new correspondence status enum.
	 *
	 * @param secPermission
	 * @param value the value
	 */
	AnnotationsPermissionsEnum(String annotationsPermission, int value)
	{
		this.annotationsPermission = annotationsPermission;
		this.value = value;
	}

	/**
	 * Gets the Sec Permission.
	 *
	 * @return the secPermission
	 */
	public String getAnnotationsPermission()
	{
		return annotationsPermission;
	}


	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue()
	{
		return value;
	}

}
