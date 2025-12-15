import { defineStore } from 'pinia'
const useDragStore = defineStore('drag', {
    state: () => {
        type state = {
            dragingElement: HTMLElement | null
        }
        return {
            dragingElement: null,
        } as state
    },
    actions: {
        startDrag(dragingElement: HTMLElement) {
            this.dragingElement = dragingElement
        },
        getDragingElement() {
            if (this.dragingElement)
                return this.dragingElement
            else
                throw new Error('no draging element')
        },
        startDragFromCopy(dragingElement: HTMLElement) {
            this.dragingElement = dragingElement.cloneNode(true) as HTMLElement
        },
        getDragingElementFromCopy() {
            if (this.dragingElement)
                return this.dragingElement.cloneNode(true) as HTMLElement
            else
                throw new Error('no draging element')
        },
        move(placeElement: HTMLElement) {
            if (placeElement === this.dragingElement) return this.dragingElement
            if (this.dragingElement) {
                if (this.dragingElement.parentElement)
                    this.dragingElement.parentElement.removeChild(this.dragingElement)
                placeElement.appendChild(this.dragingElement)
                return this.dragingElement
            } else {
                throw new Error('no draging element')
            }
        },
        moveFromCopy(placeElement: HTMLElement) {
            if (placeElement === this.dragingElement) return this.dragingElement
            if (this.dragingElement) {
                const element = this.getDragingElementFromCopy()
                placeElement.appendChild(element)
                element.ondragstart = e => this.startDrag(e.target as HTMLDivElement)
                return element
            } else {
                throw new Error('no draging element')
            }
        },
        remove() {
            if (this.dragingElement) {
                this.dragingElement.remove()
                this.dragingElement = null
            } else {
                throw new Error('no draging element')
            }
        }
    }
})
export default useDragStore
