/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.core.commons.services.commentAndRating.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.OLATResourceableRating;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.commentAndRating.model.UserCommentsCount;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.ims.cp.ui.VFSMediaFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * User interface controller and manager factory for the comment and rating
 * service. This is a spring prototype. Use the init() methods after getting
 * your instance from spring to configure the service for your resource. (See
 * the interface for an code example)
 * <P>
 * Initial Date: 24.11.2009 <br>
 *
 * @author gnaegi
 */
@Service
public class CommentAndRatingServiceImpl implements CommentAndRatingService {

	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private UserCommentsDAO userCommentsDao;

	@Override
	public Long countRatings(OLATResourceable ores, String resSubPath) {
		return Long.valueOf(userRatingsDao.countRatings(ores, resSubPath));
	}

	@Override
	public List<UserRating> getAllRatings(OLATResourceable ores, String resSubPath) {
		return userRatingsDao.getAllRatings(ores, resSubPath);
	}

	@Override
	public Float calculateRatingAverage(OLATResourceable ores, String resSubPath) {
		return userRatingsDao.getRatingAverage(ores, resSubPath);
	}

	@Override
	public UserRating createRating(Identity creator, OLATResourceable ores, String resSubPath, int ratingValue) {
		return userRatingsDao.createRating(creator, ores, resSubPath, ratingValue);
	}

	@Override
	public UserRating getRating(Identity identity, OLATResourceable ores, String resSubPath) {
		return userRatingsDao.getRating(identity, ores, resSubPath);
	}

	@Override
	public Integer getRatingValue(Identity identity, OLATResourceable ores, String resSubPath) {
		return userRatingsDao.getRatingValue(identity, ores, resSubPath);
	}

	@Override
	public UserRating reloadRating(UserRating rating) {
		return userRatingsDao.reloadRating(rating);
	}

	@Override
	public UserRating updateRating(UserRating rating, int newRatingValue) {
		return userRatingsDao.updateRating(rating, newRatingValue);
	}

	@Override
	public List<OLATResourceableRating> getMostRatedResourceables(OLATResourceable ores, int maxResults) {
		return userRatingsDao.getMostRatedResourceables(ores, maxResults);
	}

	@Override
	public long countComments(OLATResourceable ores, String resSubPath) {
		return userCommentsDao.countComments(ores, resSubPath);
	}

	@Override
	public List<UserCommentsCount> countCommentsWithSubPath(OLATResourceable ores, String resSubPath) {
		return userCommentsDao.countCommentsWithSubPath(ores, resSubPath);
	}

	@Override
	public List<UserComment> getComments(OLATResourceable ores, String resSubPath) {
		return userCommentsDao.getComments(ores, resSubPath);
	}

	@Override
	public UserComment createComment(Identity creator, OLATResourceable ores,
			String resSubPath, String commentText) {
		return userCommentsDao.createComment(creator, ores, resSubPath, commentText);
	}

	@Override
	public UserComment reloadComment(UserComment comment) {
		return userCommentsDao.reloadComment(comment);
	}

	@Override
	public UserComment replyTo(UserComment originalComment, Identity creator, String replyCommentText) {
		return userCommentsDao.replyTo(originalComment, creator, replyCommentText);
	}

	@Override
	public UserComment updateComment(UserComment comment, String newCommentText) {
		return userCommentsDao.updateComment(comment, newCommentText);
	}

	@Override
	public int deleteComment(UserComment comment, boolean deleteReplies) {
		return userCommentsDao.deleteComment(comment, deleteReplies);
	}

	@Override
	public int deleteAll(OLATResourceable ores, String resSubPath) {
		if (ores == null) {
			throw new AssertException("CommentAndRatingService must be initialized first, call init method");
		}
		int delCount = userCommentsDao.deleteAllComments(ores, resSubPath);
		delCount += userRatingsDao.deleteAllRatings(ores, resSubPath);
		return delCount;
	}

	@Override
	public VFSContainer getCommentContainer(UserComment comment) {
		String pathToCommentDir = "/comments/" + comment.getResId() + "/" + comment.getKey() + "/";
		return VFSManager.olatRootContainer(pathToCommentDir, null);
	}

	@Override
	public List<VFSLeaf> getCommentLeafs(UserComment comment) {
		VFSContainer container = getCommentContainer(comment);
		List<VFSLeaf> files = new ArrayList<>();

		VFSMediaFilter mediaFilter = new VFSMediaFilter(false);
		List<VFSItem> items = container.getItems(mediaFilter);
		for (VFSItem item : items) {
			if (item instanceof VFSLeaf leaf) {
				files.add(leaf);
			}
		}
		if (!files.isEmpty()) {
			return files;
		}
		return Collections.emptyList();
	}

	@Override
	public int deleteAllIgnoringSubPath(OLATResourceable ores) {
		if (ores == null) {
			throw new AssertException("CommentAndRatingService must be initialized first, call init method");
		}
		int delCount = userCommentsDao.deleteAllCommentsIgnoringSubPath(ores);
		delCount += userRatingsDao.deleteAllRatingsIgnoringSubPath(ores);
		return delCount;
	}
}
