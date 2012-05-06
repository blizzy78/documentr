package de.blizzy.documentr.pagestore;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.repository.RepositoryNotFoundException;

@Component
public class DefaultPageBranchResolver implements IPageBranchResolver {
	@Autowired
	private PageStore pageStore;
	
	// 1.2.3 -> 1.2.x -> 1.2 -> 1.x -> 1
	@Override
	public String resolvePageBranch(String projectName, String branchName, String pagePath) throws IOException {
		List<String> paths = null;
		try {
			paths = pageStore.listPagePaths(projectName, branchName);
		} catch (RepositoryNotFoundException e) {
			// okay
		}
		if ((paths != null) && paths.contains(pagePath)) {
			return branchName;
		}

		if (branchName.contains(".")) { //$NON-NLS-1$
			String parentBranchName = StringUtils.substringBeforeLast(branchName, "."); //$NON-NLS-1$
			branchName = StringUtils.substringAfterLast(branchName, "."); //$NON-NLS-1$
			if (branchName.equals("x")) { //$NON-NLS-1$
				return resolvePageBranch(projectName, parentBranchName, pagePath);
			}
			return resolvePageBranch(projectName, parentBranchName + ".x", pagePath); //$NON-NLS-1$
		}
		
		return null;
	}

	public void setPageStore(PageStore pageStore) {
		this.pageStore = pageStore;
	}
}
