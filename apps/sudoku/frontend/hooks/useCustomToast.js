import toast, { Toaster } from 'react-hot-toast';

const CustomToaster = () => (
    <Toaster
        position="top-center"
        reverseOrder={false}
        gutter={8}
        containerClassName=""
        containerStyle={{}}
        toastOptions={{
            // Define default options
            className: '',
            duration: 5000,
            style: {
                background: '#303958',
                color: '#fff'
            },

            // Default options for specific types
            success: {
                duration: 5000
            },
        }}
    />
)

export default () => {
    return { Toaster: CustomToaster, toast }
}