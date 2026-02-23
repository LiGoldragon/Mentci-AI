;;; mentci.el --- Emacs configuration for Mentci-AI project

;; 1. Auto-Load Disk Changes (Auto-Revert)
(global-auto-revert-mode t)
(setq auto-revert-verbose nil) ; Keep it quiet

;; 2. Read-Only Mode for Sources
;; Matches any file under the project's Sources/ directory
(defun mentci-apply-read-only-for-Sources ()
  "Force read-only mode for files in the Sources directory."
  (when (string-match-p "/Sources/" (buffer-file-name))
    (read-only-mode 1)
    (message "Mentci: Entering RO mode for system input artifact.")))

(add-hook 'find-file-hook #'mentci-apply-read-only-for-Sources)

;; 3. Git-Gutter Update Logic
(defun mentci-refresh-git-gutter ()
  "Explicitly refresh git-gutter if available."
  (when (featurep 'git-gutter)
    (git-gutter:update-all-windows)))

;; Refresh gutter when auto-revert or manual save happens
(add-hook 'after-revert-hook #'mentci-refresh-git-gutter)
(add-hook 'after-save-hook #'mentci-refresh-git-gutter)

(provide 'mentci)
;;; mentci.el ends here
