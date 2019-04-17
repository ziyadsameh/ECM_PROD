package com.ebla.viewone.services.security;
import java.util.HashMap;
import java.util.Map;

public enum DocSecurityPermissionsEnum {
	
	/** The correspondence status draft. */
	VIEW_SEC_PERMISSION("View", 1),

	/** The correspondence status progress. */
	PRINT_SEC_PERMISSION("Print", 2),

	/** The correspondence status archived. */
	PRINTW_SEC_PERMISSION("PrintW", 3),

	/** The correspondence status archived. */
	MODIFY_SEC_PERMISSION("Modify", 4);

	/** secPermission. */
	public final String secPermission;


	/** The value. */
	public final int value;

	/** The Constant map. */
	private static final Map<Integer, DocSecurityPermissionsEnum> map;
	static
	{
		map = new HashMap<Integer, DocSecurityPermissionsEnum>();
		for(DocSecurityPermissionsEnum v : DocSecurityPermissionsEnum.values())
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
	public static DocSecurityPermissionsEnum findByKey(int i)
	{
		return map.get(i);
	}

	/**
	 * Instantiates a new correspondence status enum.
	 *
	 * @param secPermission
	 * @param value the value
	 */
	DocSecurityPermissionsEnum(String secPermission, int value)
	{
		this.secPermission = secPermission;
		this.value = value;
	}

	/**
	 * Gets the Sec Permission.
	 *
	 * @return the secPermission
	 */
	public String getSecPermission()
	{
		return secPermission;
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
