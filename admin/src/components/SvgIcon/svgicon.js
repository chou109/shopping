import {
    CaretBottom,
    CaretRight,
    CircleCheck,
    CircleClose,
    Close,
    Delete,
    DocumentAdd,
    DocumentCopy,
    Download,
    Edit,
    Key,
    Menu,
    Minus,
    Operation,
    PictureFilled,
    Plus,
    QuestionFilled,
    Refresh,
    RefreshLeft,
    RefreshRight,
    Search,
    Sort,
    Unlock,
    Upload,
    User,
    View
} from '@element-plus/icons-vue'

const components = [
    CaretBottom, CaretRight, CircleCheck, CircleClose, Close, Delete,
    DocumentAdd, DocumentCopy, Download, Edit, Key, Menu, Minus,
    Operation, PictureFilled, Plus, QuestionFilled, Refresh, RefreshLeft,
    RefreshRight, Search, Sort, Unlock, Upload, User, View
]

export default {
    install: (app) => {
        for (const componentConfig of components) {
            app.component(componentConfig.name, componentConfig);
        }
    },
};
