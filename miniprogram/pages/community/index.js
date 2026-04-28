const { request, uploadFile, resolveFileUrl } = require('../../utils/request');
const mock = require('../../utils/mock');
const { navigateBack } = require('../../utils/nav');

function pad(value) {
  return String(value).padStart(2, '0');
}

function formatTime(value) {
  if (!value) {
    return '';
  }

  if (Array.isArray(value)) {
    const [year, month, day, hour, minute] = value;
    return `${year}-${pad(month)}-${pad(day)} ${pad(hour || 0)}:${pad(minute || 0)}`;
  }

  return String(value).replace('T', ' ').slice(0, 16);
}

function normalizeComment(comment) {
  return Object.assign({}, comment, {
    createdAt: formatTime(comment.createdAt)
  });
}

function normalizePost(post) {
  return Object.assign({}, post, {
    createdAt: formatTime(post.createdAt),
    liked: !!post.liked,
    coverPreview: resolveFileUrl(post.coverImage),
    commentsVisible: false,
    commentsLoaded: false,
    commentDraft: '',
    comments: (post.commentsPreview || []).map(normalizeComment)
  });
}

Page({
  data: {
    allPosts: [],
    posts: [],
    activeTab: '热门',
    posting: false,
    uploadingCover: false,
    composer: {
      content: '',
      topicTags: '训练打卡,FitNote',
      postType: 'TRAINING',
      coverImage: '',
      coverPreview: ''
    }
  },

  handleBack() {
    navigateBack('/pages/home/index');
  },

  onShow() {
    this.loadPosts();
  },

  loadPosts() {
    request({
      url: '/community/posts',
      mockData: mock.communityPosts
    }).then((posts) => {
      this.setData({
        allPosts: (posts || []).map(normalizePost)
      }, () => {
        this.applyTabFilter();
      });
    });
  },

  switchTab(event) {
    this.setData({ activeTab: event.currentTarget.dataset.tab }, () => {
      this.applyTabFilter();
    });
  },

  applyTabFilter() {
    const app = getApp();
    const currentUser = app.globalData.user || wx.getStorageSync('fitnote_user') || {};
    let posts = (this.data.allPosts || []).slice();
    if (this.data.activeTab === '关注') {
      posts = posts.filter((post) => post.liked || post.authorName === currentUser.nickname);
    }
    this.setData({ posts });
  },

  updateComposerField(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({
      [`composer.${field}`]: event.detail.value
    });
  },

  chooseCoverImage() {
    if (this.data.uploadingCover) {
      return;
    }

    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const file = res.tempFiles && res.tempFiles[0];
        if (!file) {
          return;
        }

        this.setData({
          uploadingCover: true,
          'composer.coverPreview': file.tempFilePath
        });

        uploadFile({
          url: '/files/upload',
          filePath: file.tempFilePath,
          formData: {
            category: 'community'
          },
          mockData: {
            url: file.tempFilePath
          }
        }).then((uploadRes) => {
          this.setData({
            'composer.coverImage': uploadRes.url,
            'composer.coverPreview': resolveFileUrl(uploadRes.url)
          });
          wx.showToast({ title: '图片已上传', icon: 'success' });
        }).catch(() => {
          this.setData({
            'composer.coverImage': '',
            'composer.coverPreview': ''
          });
          wx.showToast({ title: '图片上传失败', icon: 'none' });
        }).finally(() => {
          this.setData({ uploadingCover: false });
        });
      }
    });
  },

  clearCoverImage() {
    this.setData({
      'composer.coverImage': '',
      'composer.coverPreview': ''
    });
  },

  quickPost() {
    if (this.data.posting) {
      return;
    }

    const content = String(this.data.composer.content || '').trim();
    if (!content) {
      wx.showToast({ title: '先写一点训练内容吧', icon: 'none' });
      return;
    }

    this.setData({ posting: true });
    request({
      url: '/community/posts',
      method: 'POST',
      data: {
        content,
        coverImage: this.data.composer.coverImage,
        postType: this.data.composer.postType,
        topicTags: this.data.composer.topicTags
      },
      mockData: { created: true, postId: Date.now() }
    }).then(() => {
      this.setData({
        composer: {
          content: '',
          topicTags: '训练打卡,FitNote',
          postType: 'TRAINING',
          coverImage: '',
          coverPreview: ''
        }
      });
      this.loadPosts();
      wx.showToast({ title: '动态已发布', icon: 'success' });
    }).finally(() => {
      this.setData({ posting: false });
    });
  },

  toggleLike(event) {
    const postId = event.currentTarget.dataset.id;
    const currentPost = this.findPost(postId);
    if (!currentPost) {
      return;
    }

    request({
      url: `/community/posts/${postId}/like`,
      method: 'POST',
      mockData: {
        liked: !currentPost.liked,
        likeCount: currentPost.likeCount + (currentPost.liked ? -1 : 1)
      }
    }).then((res) => {
      this.updatePost(postId, (post) => Object.assign({}, post, {
        liked: res.liked,
        likeCount: res.likeCount
      }));
    });
  },

  toggleComments(event) {
    const postId = event.currentTarget.dataset.id;
    const currentPost = this.findPost(postId);
    if (!currentPost) {
      return;
    }

    const nextVisible = !currentPost.commentsVisible;
    this.updatePost(postId, (post) => Object.assign({}, post, {
      commentsVisible: nextVisible
    }));

    if (nextVisible) {
      this.fetchComments(postId);
    }
  },

  fetchComments(postId) {
    request({
      url: `/community/posts/${postId}/comments`,
      mockData: mock.communityComments[String(postId)] || []
    }).then((comments) => {
      this.updatePost(postId, (post) => Object.assign({}, post, {
        commentsLoaded: true,
        comments: (comments || []).map(normalizeComment),
        commentCount: Math.max(post.commentCount || 0, (comments || []).length)
      }));
    });
  },

  handleCommentInput(event) {
    const postId = event.currentTarget.dataset.id;
    this.updatePost(postId, (post) => Object.assign({}, post, {
      commentDraft: event.detail.value
    }));
  },

  submitComment(event) {
    const postId = event.currentTarget.dataset.id;
    const currentPost = this.findPost(postId);
    if (!currentPost) {
      return;
    }

    const content = String(currentPost.commentDraft || '').trim();
    if (!content) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }

    const app = getApp();
    const currentUser = app.globalData.user || wx.getStorageSync('fitnote_user') || {};
    const optimisticComment = {
      id: Date.now(),
      authorName: currentUser.nickname || '我',
      content,
      createdAt: formatTime(new Date().toISOString())
    };

    request({
      url: `/community/posts/${postId}/comments`,
      method: 'POST',
      data: { content },
      mockData: {
        created: true,
        commentId: Date.now(),
        commentCount: (currentPost.commentCount || 0) + 1
      }
    }).then((res) => {
      this.updatePost(postId, (post) => Object.assign({}, post, {
        commentsVisible: true,
        commentsLoaded: true,
        commentDraft: '',
        commentCount: res.commentCount || ((post.commentCount || 0) + 1),
        comments: (post.comments || []).concat([optimisticComment])
      }));
      wx.showToast({ title: '评论成功', icon: 'success' });
    });
  },

  updatePost(postId, updater) {
    const allPosts = (this.data.allPosts || []).map((post) => {
      if (post.id !== postId) {
        return post;
      }
      return updater(post);
    });

    this.setData({ allPosts }, () => {
      this.applyTabFilter();
    });
  },

  findPost(postId) {
    return (this.data.allPosts || []).find((post) => post.id === postId);
  }
});
