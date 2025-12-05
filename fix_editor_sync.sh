#!/bin/bash

# Script để fix lỗi editor không sync với file trên disk
# Chạy script này để đảm bảo tất cả files đều sync với git

echo "=========================================="
echo "Đang đồng bộ files với git repository..."
echo "=========================================="

# Kiểm tra git status
echo "1. Kiểm tra git status..."
git status --short

# Discard tất cả thay đổi chưa commit
echo ""
echo "2. Đang discard tất cả thay đổi chưa commit..."
git checkout -- .

# Xóa các file untracked (nếu cần)
echo ""
echo "3. Đang xóa các file untracked..."
git clean -fd

echo ""
echo "=========================================="
echo "✅ Hoàn tất!"
echo "=========================================="
echo ""
echo "Bây giờ hãy:"
echo "1. Đóng tất cả files đang mở trong editor"
echo "2. Reload lại workspace/project"
echo "3. Mở lại các files cần thiết"
echo ""
echo "Hoặc trong editor, sử dụng:"
echo "- 'Revert File' để reload file từ disk"
echo "- 'Accept Current Changes' để overwrite file trên disk"

