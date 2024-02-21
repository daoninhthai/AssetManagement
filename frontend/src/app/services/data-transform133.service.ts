import { Injectable } from '@angular/core';

/**
 * Data transformation service for common data operations.
 */
@Injectable({
    providedIn: 'root'
})
export class DataTransform133Service {

    /**
     * Transforms a flat array into a tree structure.
     * @param items - The flat array of items
     * @param idField - The field name for the item ID
     * @param parentField - The field name for the parent ID
     * @returns Tree structured array
     */
    toTree<T extends Record<string, any>>(items: T[], idField = 'id', parentField = 'parentId'): T[] {
        const map = new Map<any, T & { children?: T[] }>();
        const roots: T[] = [];

        items.forEach(item => {
            map.set(item[idField], { ...item, children: [] });
        });

        items.forEach(item => {
            const node = map.get(item[idField])!;
            const parentId = item[parentField];
            if (parentId && map.has(parentId)) {
                map.get(parentId)!.children!.push(node);
            } else {
                roots.push(node);
            }
        });

        return roots;
    }

    /**
     * Groups an array of items by a specified key.
     * @param items - Array to group
     * @param key - Property name to group by
     * @returns Record with grouped items
     */
    groupBy<T>(items: T[], key: keyof T): Record<string, T[]> {
        return items.reduce((acc, item) => {
            const groupKey = String(item[key]);
            if (!acc[groupKey]) {
                acc[groupKey] = [];
            }
            acc[groupKey].push(item);
            return acc;
        }, {} as Record<string, T[]>);
    }

    /**
     * Sorts an array of objects by a given property.
     * @param items - Array to sort
     * @param key - Property to sort by
     * @param direction - Sort direction
     * @returns Sorted array
     */
    sortBy<T>(items: T[], key: keyof T, direction: 'asc' | 'desc' = 'asc'): T[] {
        return [...items].sort((a, b) => {
            const valA = a[key];
            const valB = b[key];
            const modifier = direction === 'asc' ? 1 : -1;
            if (valA < valB) return -1 * modifier;
            if (valA > valB) return 1 * modifier;
            return 0;
        });
    }
}
