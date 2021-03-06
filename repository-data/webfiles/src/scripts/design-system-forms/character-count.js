'use strict';

class CharacterCount {
    constructor(field) {
        this.field = field;
        this.inputElement = this.field.querySelector('input, textarea');
        this.threshold = this.field.dataset.threshold ? this.field.dataset.threshold * 0.01 : 0;
    }

    init() {
        if (!this.inputElement) {
            return;
        }

        this.setMaxLength();

        if (!this.maxLength) {
            return;
        }

        // dynamically create the message element
        this.messageElement = document.createElement('div');
        this.messageElement.setAttribute('aria-live', 'polite');
        this.messageElement.classList.add('ds_input__message', 'ds_hint-text');
        this.messageElement.innerText = `You can enter up to ${this.maxLength} characters`;
        if (this.inputElement.value.length < this.maxLength * this.threshold) {
            this.messageElement.classList.add('hidden', 'hidden--hard');
        }
        this.field.appendChild(this.messageElement);

        this.inputElement.addEventListener('keyup', this.checkIfChanged.bind(this));
    }

    setMaxLength() {
        if (this.inputElement.getAttribute('maxlength')) {
            this.maxLength = parseInt(this.inputElement.getAttribute('maxlength'), 10);
            this.inputElement.removeAttribute('maxlength');
        } else {
            this.maxLength = this.field.dataset.maxlength;
        }
    }

    /*
     * Per GDS:
     * "Speech recognition software such as Dragon NaturallySpeaking will modify the
     * fields by directly changing its `value`. These changes don't trigger events
     * in JavaScript, so we need to poll to handle when and if they occur."
     */
    checkIfChanged() {
        if (!this.inputElement.oldValue) {
            this.inputElement.oldValue = '';
        }
        if (this.inputElement.value !== this.inputElement.oldValue) {
            this.inputElement.oldValue = this.inputElement.value;
            this.updateCountMessage.bind(this)();
        }
    }

    updateCountMessage() {
        const count = this.maxLength - this.inputElement.value.length;
        let noun = 'characters';
        if (Math.abs(count) === 1) {
            noun = 'character';
        }
        this.messageElement.innerText = `You have ${count} ${noun} remaining`;
        if (count < 0) {
            this.inputElement.classList.add('ds_input--error');
            this.inputElement.setAttribute('aria-invalid', true);
            this.messageElement.innerText = `You have ${Math.abs(count)} ${noun} too many`;
            this.messageElement.classList.remove('ds_hint-text');
        }
        else {
            this.inputElement.classList.remove('ds_input--error');
            this.inputElement.setAttribute('aria-invalid', false);
            this.messageElement.innerText = `You have ${count} ${noun} remaining`;
            this.messageElement.classList.add('ds_hint-text');
        }

        if (this.inputElement.value.length < this.maxLength * this.threshold) {
            this.messageElement.classList.add('hidden');
        } else {
            this.messageElement.classList.remove('hidden');
        }
    }
}

export default CharacterCount;
