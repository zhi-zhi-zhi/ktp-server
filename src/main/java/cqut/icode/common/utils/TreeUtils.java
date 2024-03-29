package cqut.icode.common.utils;

import cqut.icode.common.dto.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * 将权限表转为树结构返回给前端
 * @author tq
 */
public class TreeUtils {

    public static <T> List<Tree<T>> build(List<Tree<T>> nodes) {
        if (nodes == null) {
            return null;
        }

        List<Tree<T>> tree = new ArrayList<>();
        nodes.forEach(children -> {
            Long pid = children.getParentId();
            if (pid == null || pid.equals(0L)) {
                //是父节点
                tree.add(children);
                return;
            }
            for (Tree<T> parent : nodes) {
                Long id = parent.getId();
                if (id != null && id.equals(pid)) {
                    //说明是该节点是children的父节点
                    children.setHasParent(true);
                    parent.setHasChildren(true);
                    parent.getChildren().add(children);
                    return;
                }
            }
        });
        return tree;
    }
}
