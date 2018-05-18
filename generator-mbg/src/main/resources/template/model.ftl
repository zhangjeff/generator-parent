package ${package};

import ${baseDomain};
import ${paginationPackage};

public class ${domainName} extends Base${domainName} {
	
	private Pagination pagination;

	/**
	 * @return the pagination
	 */
	public Pagination getPagination() {
		return pagination;
	}

	/**
	 * @param pagination
	 *            the pagination to set
	 */
	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}
}
