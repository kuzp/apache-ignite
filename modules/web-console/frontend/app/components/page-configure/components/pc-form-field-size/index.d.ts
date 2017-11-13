declare namespace ig.config.formFieldSize {
    interface ISizeTypeOption {
        label: string,
        value: number
    }
    type ISizeType = Array<ISizeTypeOption>
    interface ISizeTypes {
        [name: string]: ISizeType
    }
}